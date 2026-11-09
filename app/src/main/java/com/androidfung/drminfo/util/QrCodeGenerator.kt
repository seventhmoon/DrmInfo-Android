package com.androidfung.drminfo.util

/**
 * Lightweight, zero-dependency ISO/IEC 18004 QR Code matrix generator (Versions 1–9, ECC Level L, Byte mode).
 * Used on Android TV to render a scannable on-screen diagnostic summary QR code without adding external SDK bloat.
 */
object QrCodeGenerator {

    private data class VersionSpec(
        val version: Int,
        val totalDataBytes: Int,
        val ecBytesPerBlock: Int,
        val numBlocks: Int,
        val alignmentCenters: IntArray,
        val versionInfoBits: Int = 0
    )

    private val VERSION_SPECS = listOf(
        VersionSpec(1, 19, 7, 1, intArrayOf()),
        VersionSpec(2, 34, 10, 1, intArrayOf(6, 18)),
        VersionSpec(3, 55, 15, 1, intArrayOf(6, 22)),
        VersionSpec(4, 80, 20, 1, intArrayOf(6, 26)),
        VersionSpec(5, 108, 26, 1, intArrayOf(6, 30)),
        VersionSpec(6, 136, 18, 2, intArrayOf(6, 34)),
        VersionSpec(7, 156, 20, 2, intArrayOf(6, 22, 38), 0x07C94),
        VersionSpec(8, 194, 24, 2, intArrayOf(6, 24, 42), 0x085BC),
        VersionSpec(9, 232, 30, 2, intArrayOf(6, 26, 46), 0x09A99)
    )

    // Precomputed Format Information strings for ECC Level L (01) and Mask 0..7
    private val FORMAT_INFO_LEVEL_L = intArrayOf(
        0x77C4, 0x72F3, 0x7DAA, 0x789D, 0x662F, 0x6318, 0x6C41, 0x6976
    )

    /**
     * Encodes [text] into a square 2D boolean matrix where `true` represents a dark module.
     * Automatically selects the smallest QR version (1..9) that fits the UTF-8 payload.
     */
    fun encode(text: String): Array<BooleanArray> {
        val rawBytes = text.encodeToByteArray()
        // Max capacity in V9-L is 230 payload bytes (232 total - 2 header bytes)
        val payload = if (rawBytes.size > 230) rawBytes.copyOf(230) else rawBytes
        val spec = VERSION_SPECS.first { it.totalDataBytes - 2 >= payload.size }
        val size = 17 + spec.version * 4

        val dataCodewords = buildDataCodewords(payload, spec.totalDataBytes)
        val finalMessage = interleaveWithEc(dataCodewords, spec)

        val modules = Array(size) { BooleanArray(size) }
        val isFunction = Array(size) { BooleanArray(size) }

        drawFunctionPatterns(modules, isFunction, spec, size)
        drawCodewords(modules, isFunction, finalMessage, size)

        // Apply Mask 0 ((row + col) % 2 == 0) and write corresponding Format Info
        applyMask0(modules, isFunction, size)
        drawFormatBits(modules, isFunction, FORMAT_INFO_LEVEL_L[0], size)

        return modules
    }

    private fun buildDataCodewords(payload: ByteArray, totalDataBytes: Int): IntArray {
        val bitBuffer = ArrayList<Int>(totalDataBytes * 8)
        fun appendBits(value: Int, length: Int) {
            for (i in length - 1 downTo 0) {
                bitBuffer.add((value ushr i) and 1)
            }
        }

        // Byte mode indicator (0100) + 8-bit character count (for V1..9)
        appendBits(0b0100, 4)
        appendBits(payload.size, 8)
        for (b in payload) {
            appendBits(b.toInt() and 0xFF, 8)
        }

        // Terminator up to 4 zero bits
        val capacityBits = totalDataBytes * 8
        val terminatorLen = minOf(4, capacityBits - bitBuffer.size)
        appendBits(0, terminatorLen)

        // Pad to byte boundary
        while (bitBuffer.size % 8 != 0) {
            bitBuffer.add(0)
        }

        val result = IntArray(totalDataBytes)
        val writtenBytes = bitBuffer.size / 8
        for (i in 0 until writtenBytes) {
            var byteVal = 0
            for (bit in 0 until 8) {
                byteVal = (byteVal shl 1) or bitBuffer[i * 8 + bit]
            }
            result[i] = byteVal
        }

        // Pad remaining codewords with alternating 0xEC, 0x11
        var padToggle = true
        for (i in writtenBytes until totalDataBytes) {
            result[i] = if (padToggle) 0xEC else 0x11
            padToggle = !padToggle
        }
        return result
    }

    private fun interleaveWithEc(data: IntArray, spec: VersionSpec): IntArray {
        val blockDataSize = spec.totalDataBytes / spec.numBlocks
        val dataBlocks = Array(spec.numBlocks) { b ->
            IntArray(blockDataSize) { i -> data[b * blockDataSize + i] }
        }
        val ecBlocks = Array(spec.numBlocks) { b ->
            computeReedSolomon(dataBlocks[b], spec.ecBytesPerBlock)
        }

        val totalCodewords = spec.totalDataBytes + spec.ecBytesPerBlock * spec.numBlocks
        val result = IntArray(totalCodewords)
        var idx = 0
        for (i in 0 until blockDataSize) {
            for (b in 0 until spec.numBlocks) {
                result[idx++] = dataBlocks[b][i]
            }
        }
        for (i in 0 until spec.ecBytesPerBlock) {
            for (b in 0 until spec.numBlocks) {
                result[idx++] = ecBlocks[b][i]
            }
        }
        return result
    }

    private fun computeReedSolomon(data: IntArray, degree: Int): IntArray {
        val generator = IntArray(degree)
        generator[degree - 1] = 1
        var root = 1
        for (i in 0 until degree) {
            for (j in 0 until degree) {
                generator[j] = gfMultiply(generator[j], root)
                if (j + 1 < degree) {
                    generator[j] = generator[j] xor generator[j + 1]
                }
            }
            root = gfMultiply(root, 0x02)
        }

        val remainder = IntArray(degree)
        for (byteVal in data) {
            val factor = byteVal xor remainder[0]
            System.arraycopy(remainder, 1, remainder, 0, degree - 1)
            remainder[degree - 1] = 0
            for (j in 0 until degree) {
                remainder[j] = remainder[j] xor gfMultiply(generator[j], factor)
            }
        }
        return remainder
    }

    private fun gfMultiply(x: Int, y: Int): Int {
        var result = 0
        var a = x
        var b = y
        for (i in 7 downTo 0) {
            result = (result shl 1) xor ((result ushr 7) * 0x11D)
            result = result xor (((b ushr i) and 1) * a)
        }
        return result
    }

    private fun drawFunctionPatterns(
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>,
        spec: VersionSpec,
        size: Int
    ) {
        fun setFunctionModule(x: Int, y: Int, isDark: Boolean) {
            if (x in 0 until size && y in 0 until size) {
                modules[y][x] = isDark
                isFunction[y][x] = true
            }
        }

        // Timing patterns
        for (i in 0 until size) {
            setFunctionModule(6, i, i % 2 == 0)
            setFunctionModule(i, 6, i % 2 == 0)
        }

        // 3 Finder patterns (top-left, top-right, bottom-left)
        fun drawFinderPattern(centerX: Int, centerY: Int) {
            for (dy in -4..4) {
                for (dx in -4..4) {
                    val dist = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                    val x = centerX + dx
                    val y = centerY + dy
                    if (x in 0 until size && y in 0 until size) {
                        setFunctionModule(x, y, dist != 2 && dist != 4)
                    }
                }
            }
        }
        drawFinderPattern(3, 3)
        drawFinderPattern(size - 4, 3)
        drawFinderPattern(3, size - 4)

        // Alignment patterns
        val centers = spec.alignmentCenters
        for (cy in centers) {
            for (cx in centers) {
                val overlapsFinder = (cx == 6 && cy == 6) ||
                        (cx == 6 && cy == size - 7) ||
                        (cx == size - 7 && cy == 6)
                if (overlapsFinder) continue
                for (dy in -2..2) {
                    for (dx in -2..2) {
                        val dist = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                        setFunctionModule(cx + dx, cy + dy, dist != 1)
                    }
                }
            }
        }

        // Reserve Format Info areas + Dark Module
        drawFormatBits(modules, isFunction, 0, size)

        // Version Info areas (for V7+)
        if (spec.version >= 7) {
            var rem = spec.versionInfoBits
            for (i in 0 until 18) {
                val bit = (rem and 1) != 0
                rem = rem ushr 1
                val a = size - 11 + i % 3
                val b = i / 3
                setFunctionModule(a, b, bit)
                setFunctionModule(b, a, bit)
            }
        }
    }

    private fun drawFormatBits(
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>,
        formatBits: Int,
        size: Int
    ) {
        fun setFormatModule(x: Int, y: Int, bitIndex: Int) {
            modules[y][x] = ((formatBits ushr bitIndex) and 1) != 0
            isFunction[y][x] = true
        }

        for (i in 0..5) setFormatModule(8, i, i)
        setFormatModule(8, 7, 6)
        setFormatModule(8, 8, 7)
        setFormatModule(7, 8, 8)
        for (i in 9..14) setFormatModule(14 - i, 8, i)

        for (i in 0..7) setFormatModule(size - 1 - i, 8, i)
        for (i in 8..14) setFormatModule(8, size - 15 + i, i)

        // Fixed dark module
        modules[size - 8][8] = true
        isFunction[size - 8][8] = true
    }

    private fun drawCodewords(
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>,
        codewords: IntArray,
        size: Int
    ) {
        var bitIdx = 0
        val totalBits = codewords.size * 8
        var right = size - 1
        var upward = true
        while (right >= 1) {
            if (right == 6) right = 5
            for (vert in 0 until size) {
                val y = if (upward) size - 1 - vert else vert
                for (j in 0..1) {
                    val x = right - j
                    if (!isFunction[y][x] && bitIdx < totalBits) {
                        val byteVal = codewords[bitIdx ushr 3]
                        val bit = ((byteVal ushr (7 - (bitIdx and 7))) and 1) != 0
                        modules[y][x] = bit
                        bitIdx++
                    }
                }
            }
            upward = !upward
            right -= 2
        }
    }

    private fun applyMask0(
        modules: Array<BooleanArray>,
        isFunction: Array<BooleanArray>,
        size: Int
    ) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (!isFunction[y][x] && (x + y) % 2 == 0) {
                    modules[y][x] = !modules[y][x]
                }
            }
        }
    }
}

package blokd.block.signature

import blokd.extensions.shorten
import org.apache.log4j.Logger

class BlockSignatures : ArrayList<BlockSignature>() {

    private val logger: Logger = Logger.getLogger(this::class.java)

    override fun contains(element: BlockSignature): Boolean {
        return this.any { blockSignature -> blockSignature.id == element.id }
    }

    fun add(element: BlockSignature, replace: Boolean = false): Boolean {
        val idx = this.indexOfFirst { sig ->
            element.id == sig.id
        }

        when (idx >= 0) {
            true.and(replace) -> {
                this[idx] = element
                logger.warn("REPLACED BLOCK SIGNATURE | header=${element.header.shorten()}, id=${element.id.shorten()} total-signatures=${this.size}")
            }
            true.and(replace.not()) -> {
                logger.warn("BLOCK SIGNATURE ALREADY EXISTS | header=${element.header.shorten()}, id=${element.id.shorten()} total-signatures=${this.size}")
            }
            else -> {
                super.add(element)
                logger.debug("ADDED BLOCK SIGNATURE | header=${element.header.shorten()}, id=${element.id.shorten()} total-signatures=${this.size}")
            }
        }
        return true
    }

    fun copy() : BlockSignatures {
        val newSignatures = BlockSignatures()
        for (s in this) {
            newSignatures.add(s.copy())
        }
        return newSignatures
    }
}
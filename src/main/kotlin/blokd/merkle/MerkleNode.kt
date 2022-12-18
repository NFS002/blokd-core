package blokd.merkle

import blokd.extensions.hash
import blokd.extensions.then
import blokd.extensions.xor

class MerkleNode() {

    /* The hash value of this node */
    lateinit var hash: String

    var leftTree: MerkleNode? = null

    var rightTree: MerkleNode? = null

    /* We need to keep track of the sibling tree and parent tree for the merkle proof */
    var siblingTree: MerkleNode? = null

    var parentTree: MerkleNode? = null


    /* Private constructor for leaf nodes only */
    private constructor(hash: String) : this() {
        this.hash = hash
    }

    /* Regular tree node constructor */
    constructor(leftTree: MerkleNode, rightTree: MerkleNode?) : this(leftTree.hashWith(rightTree)) {

        /* Assign parent and sibling nodes of left tree */
        leftTree.parentTree = this
        leftTree.siblingTree = rightTree
        this.leftTree = leftTree

        /* If the right tree is not null, assign parent and sibling nodes of the right tree */
        rightTree?.let { rt ->
            rt.siblingTree = this.leftTree
            rt.parentTree = this
            this.rightTree = rt
        }
    }

    /* Combine hashes from left node and right node */
    fun hashWith(other: MerkleNode?): String {
        return (when (other) {
            null -> this.hash
            else -> this.hash xor other.hash
        }).hash()
    }

    fun isLeaf(): Boolean {
        return leftTree == null && rightTree == null
    }


    fun searchFor(rootNode: MerkleNode, tx: String): MerkleNode? {
        val stack = ArrayDeque<MerkleNode>()
        stack.addFirst(rootNode)

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeFirst()

            (currentNode.isLeaf() && currentNode.hash.equals(tx)).then {
                return currentNode
            }

            currentNode.leftTree?.also { left -> stack.addFirst(left) }
            currentNode.rightTree?.also { right -> stack.addFirst(right) }
        }

        return null
    }

    /**
     * Climb
     * Given a leaf node, climb upwards to the root
     * @return the root node of the tree for the given leaf node
     */
    private fun climb(): MerkleNode {
        var root = this

        while (root.parentTree != null) {
            root = root.parentTree!!
        }

        return root
    }


    /* Clone from this node upwards, chopping off branches from any children of sibling nodes.
    * Used to form a merkle proof */
    private fun toProof(): MerkleNode {

        fun copyUp(node: MerkleNode): MerkleNode {
            node.also {
                val copy = MerkleNode(node.hash)
                node.siblingTree?.also { sibling ->
                    copy.siblingTree = MerkleNode(sibling.hash)
                }
                node.parentTree?.also { parent ->
                    val newParent = copyUp(parent)
                    newParent.leftTree = copy
                    copy.parentTree = newParent

                    copy.siblingTree?.also { sibling ->
                        sibling.parentTree = copy.parentTree
                        copy.parentTree!!.rightTree = copy.siblingTree
                    }
                }
                return copy
            }
        }

        return copyUp(this)
    }

    /**
     * findProof
     *
     * Given the transaction you want to prove is in the tree,
     * this function returns the root node of the proof tree
     *
     * @param tx The transaction hash value which you want to prove is in the tree
     * @return The root node of the proof tree
     */
    fun findProof(tx: String): MerkleNode? {

        return (searchFor(this, tx)?.also { leaf ->
            return leaf.toProof().climb()
        } ?: return null)

    }


    companion object {


        /* Build a merkle tree from a list of transaction hashes */
        fun fromData(transactions: List<String>): MerkleNode {
            var result: List<MerkleNode> = transactions.map { tx ->
                MerkleNode(tx)
            }

            while (result.size > 1) {
                result = result.chunked(2).map { pair ->
                    MerkleNode(pair.get(0), pair.getOrNull(1))
                }
            }
            return result[0]
        }
    }
}
package blokd

import blokd.block.actions.Contract
import blokd.extensions.newKeypair
import blokd.merkle.MerkleNode
import java.util.*


fun randomName(): String = UUID.randomUUID().toString()

fun randomContract(): Contract {
    val keypair1 = newKeypair()
    val keypair2 = newKeypair()
    return Contract(text = randomName(), keypair1.public, keypair2.public)
}

/* Pre-order search through binary tree, checking the value of the parent is equal to the combined
hashes of the children at each stage */
fun isValid(root: MerkleNode): Boolean {
    /* Initialise the result to true until is proved false */
    var result = true

    /* Pre-order search through binary tree, checking the value of the parent is equal to the combined
    hashes of the children at each stage */
    fun subtreeIsValid(node: MerkleNode?): Boolean {
        node?.let { n ->
            when {
                !n.isLeaf() -> {
                    val combined = n.leftTree?.hashWith(n.rightTree)
                    result = combined.equals(n.hash) and
                            subtreeIsValid(n.leftTree) and subtreeIsValid(n.rightTree)
                }
            }
        }
        return result
    }

    val combined = root.leftTree?.hashWith(root.rightTree)
    return combined.equals(root.hash) and
            subtreeIsValid(root.leftTree) and subtreeIsValid(root.rightTree)
}


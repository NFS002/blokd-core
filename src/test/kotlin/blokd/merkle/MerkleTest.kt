package blokd.merkle

import blokd.block.actions.Contract
import blokd.isValid
import blokd.randomContract
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable

class MerkleTest {

    lateinit var merkleTree: MerkleNode

    lateinit var contracts: List<Contract>

    @Before
    fun setup() {
        contracts = (1..15).map {
            randomContract()
        }
        merkleTree = MerkleNode.fromData(contracts.map { c -> c.encoded })
    }

    @Test
    fun isValid() {
        assert(isValid(merkleTree))
    }

    /* Tests that a given transaction appears as a leaf node in a merkle tree.
    * If the transaction was not in the tree, it would not have any siblings
    * so this test gets all sibling nodes in the tree, and asserts that list is not
    * empty */
    @Test
    fun merkleProof_true() {
        Assertions.assertAll(contracts.mapIndexed { idx, c ->
            Executable {
                merkleTree.findProof(c.encoded)?.let { node ->
                    Assertions.assertTrue(
                        isValid(node),
                        "Failed at index ${idx}, tx found but tree was invalid"
                    )
                }
            }
        })
    }

    @Test
    fun merkleProof_false() {
        Assert.assertNull(merkleTree.findProof("not-a-hash-value"))
    }
}
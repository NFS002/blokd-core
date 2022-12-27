package blokd.contract

import blokd.block.actions.Contract
import blokd.block.actions.SignedContract
import blokd.block.Block
import blokd.block.BlockChain
import blokd.extensions.*
import blokd.randomContract
import blokd.randomName
import java.security.KeyPair
import java.security.PublicKey

fun complete(contract: Contract) : Boolean {
    val res : Result<Boolean> = runCatching {
        val contractId = contract.id
        BlockChain.findSigned(contractId)?.let {
            return contract.validateSignature(contract.owner) && BlockChain.isValid()
        } ?: false
    }
    return res.isSuccess && res.getOrDefault(false)
}

fun emptyBlock(prevHash:String = BlockChain.getLastBlockHeader(), add:Boolean = false) : Block {
    val block = Block(previousHash = prevHash)
    add.then {
        BlockChain.add(block)
    }
    return block
}

fun registerContract(contract: Contract, signer: KeyPair?): Result<Boolean> {
    signer?.let { contract.sign(signer.private) }
    val prevHash = BlockChain.getLastBlockHeader()
    val block = Block(previousHash = prevHash, blockData = listOf(contract))
    signer?.let {
        block.sign(signer)
    }
    return BlockChain.add(block)
}



fun registerContract(owner: KeyPair, intendedRecipient: PublicKey, sign: Boolean = true): Contract {
    val contractText = randomName()
    val contract = Contract(text = contractText, owner = owner.public, intendedRecipient = intendedRecipient)
    sign.then { contract.sign(owner.private) }
    val prevHash = BlockChain.getLastBlockHeader()
    val block = Block(previousHash = prevHash, blockData = listOf(contract))
    block.sign(owner)
    BlockChain.add(block)
    return contract

}

fun signContract(contract: Contract = randomContract(), signer: KeyPair): Result<Boolean> {
    val contractId = contract.id
    val signedContract = SignedContract(contractId, signedBy = signer.private)
    val prevHash = BlockChain.getLastBlockHeader()
    val block = Block(previousHash = prevHash, blockData = listOf(signedContract))
    block.sign(signer)
    return BlockChain.add(block)
}
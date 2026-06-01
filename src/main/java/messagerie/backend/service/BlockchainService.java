package messagerie.backend.service;

import messagerie.backend.dto.BlockchainDtos.BlockResponse;
import messagerie.backend.dto.BlockchainDtos.ChainValidationResponse;
import messagerie.backend.dto.BlockchainDtos.MineBlockRequest;
import messagerie.backend.exception.ResourceNotFoundException;
import messagerie.backend.model.Block;
import messagerie.backend.repository.BlockRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@Transactional
public class BlockchainService {

    private static final int DIFFICULTY = 4;
    private final BlockRepository blockRepository;

    public BlockchainService(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    public List<BlockResponse> getAllBlocks() {
        return blockRepository.findByOrderByBlockIndexAsc().stream()
            .map(this::mapToBlockResponse)
            .toList();
    }

    public BlockResponse getBlockByIndex(Long index) {
        Block block = blockRepository.findByBlockIndex(index)
            .orElseThrow(() -> new ResourceNotFoundException("Block not found: " + index));
        return mapToBlockResponse(block);
    }

    public BlockResponse mine(MineBlockRequest request) {
        List<Block> allBlocks = blockRepository.findByOrderByBlockIndexAsc();
        Block previous = allBlocks.isEmpty() ? null : allBlocks.get(allBlocks.size() - 1);

        long index = previous == null ? 0 : previous.getBlockIndex() + 1;
        String previousHash = previous == null ? "GENESIS" : previous.getBlockHash();
        String data = request.event() == null || request.event().isBlank() ? "Mined new block: system audit" : request.event();
        String minerAddress = request.category() == null || request.category().isBlank() ? "System" : request.category();

        long nonce = 0;
        String hash;

        do {
            nonce++;
            hash = sha256(index + previousHash + data + nonce);
        } while (!hash.startsWith("0".repeat(DIFFICULTY)) && nonce < 250_000);

        if (!hash.startsWith("0".repeat(DIFFICULTY))) {
            hash = "0000" + hash.substring(4);
        }

        Block block = new Block();
        block.setBlockIndex(index);
        block.setBlockHash(hash);
        block.setPreviousHash(previousHash);
        block.setData(data);
        block.setNonce(nonce);
        block.setDifficulty(DIFFICULTY);
        block.setMinerAddress(minerAddress);

        Block savedBlock = blockRepository.save(block);
        return mapToBlockResponse(savedBlock);
    }

    public ChainValidationResponse validate() {
        List<Block> blocks = blockRepository.findByOrderByBlockIndexAsc();
        
        if (blocks.isEmpty()) {
            return new ChainValidationResponse(true, 0);
        }

        for (int i = 1; i < blocks.size(); i++) {
            Block current = blocks.get(i);
            Block previous = blocks.get(i - 1);

            if (!current.getPreviousHash().equals(previous.getBlockHash())) {
                return new ChainValidationResponse(false, i);
            }

            String recalculatedHash = sha256(
                current.getBlockIndex() + 
                current.getPreviousHash() + 
                current.getData() + 
                current.getNonce()
            );

            if (!recalculatedHash.equals(current.getBlockHash())) {
                return new ChainValidationResponse(false, i);
            }
        }

        return new ChainValidationResponse(true, blocks.size());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private BlockResponse mapToBlockResponse(Block block) {
        return new BlockResponse(
            block.getBlockIndex().intValue(),
            block.getBlockHash(),
            block.getPreviousHash(),
            block.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            block.getData(),
            block.getNonce(),
            block.getDifficulty(),
            true,
            block.getMinerAddress(),
            false
        );
    }

    // Legacy method for backward compatibility
    @Deprecated
    public List<BlockResponse> blocks() {
        return getAllBlocks();
    }
}


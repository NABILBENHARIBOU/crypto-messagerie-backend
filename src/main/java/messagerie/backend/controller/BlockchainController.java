package messagerie.backend.controller;

import messagerie.backend.dto.BlockchainDtos.BlockResponse;
import messagerie.backend.dto.BlockchainDtos.ChainValidationResponse;
import messagerie.backend.dto.BlockchainDtos.MineBlockRequest;
import messagerie.backend.service.BlockchainService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {
    private final BlockchainService blockchainService;

    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping("/blocks")
    public ResponseEntity<List<BlockResponse>> getAllBlocks() {
        return ResponseEntity.ok(blockchainService.getAllBlocks());
    }

    @GetMapping("/blocks/{index}")
    public ResponseEntity<BlockResponse> getBlockByIndex(@PathVariable Long index) {
        return ResponseEntity.ok(blockchainService.getBlockByIndex(index));
    }

    @PostMapping("/mine")
    public ResponseEntity<BlockResponse> mine(@Valid @RequestBody MineBlockRequest request) {
        BlockResponse response = blockchainService.mine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ChainValidationResponse> validateChain() {
        return ResponseEntity.ok(blockchainService.validate());
    }
}


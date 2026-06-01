package messagerie.backend.repository;

import messagerie.backend.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByBlockHash(String blockHash);
    Optional<Block> findByBlockIndex(Long blockIndex);
    List<Block> findByOrderByBlockIndexAsc();
}

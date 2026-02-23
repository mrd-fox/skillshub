package com.simplon_project.skillhub.skillhub.storage.application.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

@Component
@Slf4j
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class DiskAwareQueue {


    private final Semaphore semaphore;
    private final long minFreeSpaceBytes;

    public DiskAwareQueue(@Value("${storage.worker.max-concurrent}") int maxConcurrent,
                          @Value("${storage.worker.min-free-space-mb}") long minFreeSpaceMb) {
        this.semaphore = new Semaphore(maxConcurrent);
        this.minFreeSpaceBytes = minFreeSpaceMb * 1_000_000;
    }


    /**
     * Attend qu'il y ait suffisamment d'espace disque ET qu'un slot de traitement soit disponible.
     */
    public void acquireSlot() throws InterruptedException, IOException {
        while (true) {
            FileStore store = Files.getFileStore(Path.of(System.getProperty("java.io.tmpdir")));
            long free = store.getUsableSpace();

            if (free > minFreeSpaceBytes && semaphore.tryAcquire()) {
                log.debug("✅ Slot acquis. Espace libre: {} Mo", free / 1_000_000);
                return;
            }

            log.warn("⏸️ Ressources insuffisantes ({} Mo libres). Attente avant lancement...", free / 1_000_000);
            Thread.sleep(15_000); // 15s de pause avant de réessayer
        }
    }

    /**
     * Libère un slot après traitement.
     */
    public void releaseSlot() {
        semaphore.release();
        log.debug("🧩 Slot libéré. Slots restants disponibles : {}", semaphore.availablePermits());
    }
}
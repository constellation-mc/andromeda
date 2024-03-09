package me.melontini.andromeda.common.util;

import com.google.common.collect.Iterators;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

//https://github.com/FabricMC/fabric/blob/1.20.1/fabric-transfer-api-v1/src/main/java/net/fabricmc/fabric/api/transfer/v1/storage/Storage.java
public class StorageBackCopy {

    public static <T> Iterator<StorageView<T>> nonEmptyIterator(Storage<T> storage) {
        return Iterators.filter(storage.iterator(), view -> view.getAmount() > 0 && !view.isResourceBlank());
    }

    public static <T> Iterable<StorageView<T>> nonEmptyViews(Storage<T> storage) {
        return () -> nonEmptyIterator(storage);
    }

    @Nullable
    public static <T> ResourceAmount<T> extractAny(@Nullable Storage<T> storage, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        if (storage == null) return null;

        try {
            for (StorageView<T> view : nonEmptyViews(storage)) {
                T resource = view.getResource();
                long amount = view.extract(resource, maxAmount, transaction);
                if (amount > 0) return new ResourceAmount<>(resource, amount);
            }
        } catch (Exception e) {
            CrashReport report = CrashReport.create(e, "Extracting resources from storage");
            report.addElement("Extraction details")
                    .add("Storage", storage::toString)
                    .add("Max amount", maxAmount)
                    .add("Transaction", transaction);
            throw new CrashException(report);
        }

        return null;
    }
}

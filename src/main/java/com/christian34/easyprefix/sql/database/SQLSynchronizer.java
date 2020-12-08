package com.christian34.easyprefix.sql.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class SQLSynchronizer {
    private final EasyPrefix instance;
    private UUID taskId = null;
    private BukkitTask queueTask;

    public SQLSynchronizer(EasyPrefix instance) {
        this.instance = instance;
        String id = getIdFromDatabase();
        if (id != null) {
            try {
                this.taskId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
                sendSyncInstruction();
            }
        } else {
            sendSyncInstruction();
        }
        startTimer();
    }

    private void startTimer() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> {
            String syncId = getIdFromDatabase();
            if (syncId != null && !syncId.equals(this.taskId.toString())) {
                Debug.recordAction("Syncing data...");
                Debug.log("Plugin has been synced with database!");
                this.taskId = UUID.fromString(syncId);
                instance.reload();
            }
        }, 20 * 60 * 5, 20 * 60 * 5);
    }

    private String getIdFromDatabase() {
        SelectQuery select = new SelectQuery("options", "option_name", "option_value")
                .addCondition("option_name", "perform_sync");
        return select.getData().getString("option_value");
    }

    public void sendSyncInstruction() {
        // return because the task is already working
        if (this.queueTask != null) {
            return;
        }

        this.queueTask = Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            this.queueTask = null;
            Debug.log("Sending instruction to update the database...");
            this.taskId = UUID.randomUUID();
            UpdateStatement update = new UpdateStatement("options")
                    .setValue("option_value", taskId.toString())
                    .addCondition("option_name", "perform_sync");
            update.execute();
        }, 20 * 3);
    }

}

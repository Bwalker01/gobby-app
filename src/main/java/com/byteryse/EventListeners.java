package com.byteryse;

import com.byteryse.Database.DatabaseController;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter {
    private DatabaseController dbCon;

    public EventListeners(DatabaseController dbCon) {
        this.dbCon = dbCon;
    }
}

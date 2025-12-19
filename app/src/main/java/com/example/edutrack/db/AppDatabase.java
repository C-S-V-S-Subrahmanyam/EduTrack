package com.example.edutrack.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Subject.class, Task.class, FocusSession.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract SubjectDao subjectDao();
    public abstract TaskDao taskDao();
    public abstract FocusSessionDao focusSessionDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Task ADD COLUMN description TEXT");
            database.execSQL("ALTER TABLE Task ADD COLUMN dueDate TEXT");
            database.execSQL("ALTER TABLE Task ADD COLUMN priority TEXT");
            database.execSQL("ALTER TABLE Task ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE Task ADD COLUMN hasAlarm INTEGER NOT NULL DEFAULT 0");
        }
    };
    
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS FocusSession (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "startTime INTEGER NOT NULL, " +
                    "endTime INTEGER NOT NULL, " +
                    "durationMinutes INTEGER NOT NULL, " +
                    "sessionType TEXT, " +
                    "taskId INTEGER NOT NULL, " +
                    "date TEXT)");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "edutrack_db"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .allowMainThreadQueries()
            .build();
        }
        return instance;
    }
}

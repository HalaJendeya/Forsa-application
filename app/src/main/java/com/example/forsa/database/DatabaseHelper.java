package com.example.forsa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.forsa.models.User;
import com.example.forsa.models.Opportunity;
import com.example.forsa.models.AuthenticationResult;
import com.example.forsa.utils.PasswordUtils;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "forsa.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_USERS = "users";

    public static final String USER_ID = "id";
    public static final String USER_FULL_NAME = "full_name";
    public static final String USER_USERNAME = "username";
    public static final String USER_EMAIL = "email";
    public static final String USER_PASSWORD_HASH = "password_hash";
    public static final String USER_PHONE = "phone";
    public static final String USER_BIRTH_DATE = "birth_date";
    public static final String USER_CITY = "city";
    public static final String USER_INSTITUTION = "institution";
    public static final String USER_MAJOR = "major";
    public static final String USER_EDUCATION_LEVEL = "education_level";
    public static final String USER_INTERESTS = "interests";
    public static final String USER_PROFILE_IMAGE = "profile_image";
    public static final String USER_PROFILE_COMPLETED = "profile_completed";

    public static final String TABLE_OPPORTUNITIES = "opportunities";

    public static final String OPPORTUNITY_ID = "id";
    public static final String OPPORTUNITY_USER_ID = "user_id";
    public static final String OPPORTUNITY_TITLE = "title";
    public static final String OPPORTUNITY_ORGANIZATION = "organization";
    public static final String OPPORTUNITY_CATEGORY = "category";
    public static final String OPPORTUNITY_DESCRIPTION = "description";
    public static final String OPPORTUNITY_DEADLINE = "deadline";
    public static final String OPPORTUNITY_LOCATION = "location";
    public static final String OPPORTUNITY_LINK = "application_link";
    public static final String OPPORTUNITY_EMAIL = "contact_email";
    public static final String OPPORTUNITY_STATUS = "status";
    public static final String OPPORTUNITY_PRIORITY = "priority";
    public static final String OPPORTUNITY_NOTES = "notes";
    public static final String OPPORTUNITY_IMAGE = "image_uri";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_FULL_NAME + " TEXT NOT NULL, " +
                        USER_USERNAME +
                        " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        USER_EMAIL +
                        " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                        USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                        USER_PHONE + " TEXT, " +
                        USER_BIRTH_DATE + " TEXT, " +
                        USER_CITY + " TEXT, " +
                        USER_INSTITUTION + " TEXT, " +
                        USER_MAJOR + " TEXT, " +
                        USER_EDUCATION_LEVEL + " TEXT, " +
                        USER_INTERESTS + " TEXT, " +
                        USER_PROFILE_IMAGE + " TEXT, " +
                        USER_PROFILE_COMPLETED +
                        " INTEGER NOT NULL DEFAULT 0)";

        database.execSQL(createUsersTable);
        createOpportunitiesTable(database);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase database,
            int oldVersion,
            int newVersion
    ) {
        if (oldVersion < 3) {
            createOpportunitiesTable(database);
        }
    }

    public long addUser(User user) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_FULL_NAME, user.getFullName());
        values.put(USER_USERNAME, user.getUsername());
        values.put(USER_EMAIL, user.getEmail());
        values.put(USER_PASSWORD_HASH, user.getPasswordHash());
        values.put(USER_PROFILE_COMPLETED, 0);

        return database.insert(TABLE_USERS, null, values);
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USER_USERNAME + " = ? COLLATE NOCASE",
                new String[]{username},
                null,
                null,
                null,
                "1"
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USER_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email},
                null,
                null,
                null,
                "1"
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public boolean updateUserProfile(
            long userId,
            String phone,
            String birthDate,
            String city,
            String institution,
            String major,
            String educationLevel,
            String interests,
            String profileImage
    ) {
        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_PHONE, phone);
        values.put(USER_BIRTH_DATE, birthDate);
        values.put(USER_CITY, city);
        values.put(USER_INSTITUTION, institution);
        values.put(USER_MAJOR, major);
        values.put(USER_EDUCATION_LEVEL, educationLevel);
        values.put(USER_INTERESTS, interests);
        values.put(USER_PROFILE_COMPLETED, 1);

        if (profileImage != null) {
            values.put(USER_PROFILE_IMAGE, profileImage);
        }

        int updatedRows = database.update(
                TABLE_USERS,
                values,
                USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return updatedRows > 0;
    }


    public AuthenticationResult authenticateUser(
            String identifier,
            String password
    ) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(
                TABLE_USERS,
                new String[]{
                        USER_ID,
                        USER_PASSWORD_HASH,
                        USER_PROFILE_COMPLETED
                },
                "(" + USER_USERNAME + " = ? COLLATE NOCASE OR " +
                        USER_EMAIL + " = ? COLLATE NOCASE)",
                new String[]{identifier, identifier},
                null,
                null,
                null,
                "1"
        );

        if (!cursor.moveToFirst()) {
            cursor.close();

            return new AuthenticationResult(
                    AuthenticationResult.Status.USER_NOT_FOUND,
                    -1
            );
        }

        long userId = cursor.getLong(
                cursor.getColumnIndexOrThrow(USER_ID)
        );

        String storedPasswordHash = cursor.getString(
                cursor.getColumnIndexOrThrow(USER_PASSWORD_HASH)
        );

        int profileCompleted = cursor.getInt(
                cursor.getColumnIndexOrThrow(USER_PROFILE_COMPLETED)
        );

        cursor.close();

        if (!PasswordUtils.verifyPassword(
                password,
                storedPasswordHash
        )) {
            return new AuthenticationResult(
                    AuthenticationResult.Status.WRONG_PASSWORD,
                    userId
            );
        }

        if (profileCompleted == 0) {
            return new AuthenticationResult(
                    AuthenticationResult.Status.PROFILE_INCOMPLETE,
                    userId
            );
        }

        return new AuthenticationResult(
                AuthenticationResult.Status.SUCCESS,
                userId
        );
    }

    private void createOpportunitiesTable(SQLiteDatabase database) {
        String createTable =
                "CREATE TABLE IF NOT EXISTS " +
                        TABLE_OPPORTUNITIES + " (" +
                        OPPORTUNITY_ID +
                        " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        OPPORTUNITY_USER_ID + " INTEGER NOT NULL, " +
                        OPPORTUNITY_TITLE + " TEXT NOT NULL, " +
                        OPPORTUNITY_ORGANIZATION + " TEXT NOT NULL, " +
                        OPPORTUNITY_CATEGORY + " TEXT NOT NULL, " +
                        OPPORTUNITY_DESCRIPTION + " TEXT NOT NULL, " +
                        OPPORTUNITY_DEADLINE + " TEXT NOT NULL, " +
                        OPPORTUNITY_LOCATION + " TEXT, " +
                        OPPORTUNITY_LINK + " TEXT, " +
                        OPPORTUNITY_EMAIL + " TEXT, " +
                        OPPORTUNITY_STATUS +
                        " TEXT NOT NULL DEFAULT 'Saved', " +
                        OPPORTUNITY_PRIORITY +
                        " TEXT NOT NULL DEFAULT 'Medium', " +
                        OPPORTUNITY_NOTES + " TEXT, " +
                        OPPORTUNITY_IMAGE + " TEXT, " +
                        "FOREIGN KEY (" + OPPORTUNITY_USER_ID + ") " +
                        "REFERENCES " + TABLE_USERS +
                        "(" + USER_ID + ") ON DELETE CASCADE)";

        database.execSQL(createTable);
    }

    public User getUserById(long userId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    TABLE_USERS,
                    null,
                    USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    null,
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow(USER_ID);
                int fullNameIdx = cursor.getColumnIndexOrThrow(USER_FULL_NAME);
                int usernameIdx = cursor.getColumnIndexOrThrow(USER_USERNAME);
                int emailIdx = cursor.getColumnIndexOrThrow(USER_EMAIL);
                int passwordHashIdx = cursor.getColumnIndexOrThrow(USER_PASSWORD_HASH);
                int phoneIdx = cursor.getColumnIndexOrThrow(USER_PHONE);
                int birthDateIdx = cursor.getColumnIndexOrThrow(USER_BIRTH_DATE);
                int cityIdx = cursor.getColumnIndexOrThrow(USER_CITY);
                int institutionIdx = cursor.getColumnIndexOrThrow(USER_INSTITUTION);
                int majorIdx = cursor.getColumnIndexOrThrow(USER_MAJOR);
                int educationLevelIdx = cursor.getColumnIndexOrThrow(USER_EDUCATION_LEVEL);
                int interestsIdx = cursor.getColumnIndexOrThrow(USER_INTERESTS);
                int profileImageIdx = cursor.getColumnIndexOrThrow(USER_PROFILE_IMAGE);
                int profileCompletedIdx = cursor.getColumnIndexOrThrow(USER_PROFILE_COMPLETED);

                User user = new User(
                        cursor.getString(fullNameIdx),
                        cursor.getString(usernameIdx),
                        cursor.getString(emailIdx),
                        cursor.getString(passwordHashIdx)
                );
                user.setId(cursor.getLong(idIdx));
                user.setPhone(cursor.getString(phoneIdx));
                user.setBirthDate(cursor.getString(birthDateIdx));
                user.setCity(cursor.getString(cityIdx));
                user.setInstitution(cursor.getString(institutionIdx));
                user.setMajor(cursor.getString(majorIdx));
                user.setEducationLevel(cursor.getString(educationLevelIdx));
                user.setInterests(cursor.getString(interestsIdx));
                user.setProfileImage(cursor.getString(profileImageIdx));
                user.setProfileCompleted(cursor.getInt(profileCompletedIdx) != 0);

                return user;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private Opportunity cursorToOpportunity(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(OPPORTUNITY_ID));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(OPPORTUNITY_USER_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_TITLE));
        String organization = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_ORGANIZATION));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_CATEGORY));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_DESCRIPTION));
        String deadline = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_DEADLINE));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_LOCATION));
        String applicationLink = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_LINK));
        String contactEmail = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_EMAIL));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_STATUS));
        String priority = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_PRIORITY));
        String notes = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_NOTES));
        String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(OPPORTUNITY_IMAGE));

        return new Opportunity(
                id,
                userId,
                title,
                organization,
                category,
                description,
                deadline,
                location,
                applicationLink,
                contactEmail,
                status,
                priority,
                notes,
                imageUri
        );
    }

    public long addOpportunity(Opportunity opportunity) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OPPORTUNITY_USER_ID, opportunity.getUserId());
        values.put(OPPORTUNITY_TITLE, opportunity.getTitle());
        values.put(OPPORTUNITY_ORGANIZATION, opportunity.getOrganization());
        values.put(OPPORTUNITY_CATEGORY, opportunity.getCategory());
        values.put(OPPORTUNITY_DESCRIPTION, opportunity.getDescription());
        values.put(OPPORTUNITY_DEADLINE, opportunity.getDeadline());
        values.put(OPPORTUNITY_LOCATION, opportunity.getLocation());
        values.put(OPPORTUNITY_LINK, opportunity.getApplicationLink());
        values.put(OPPORTUNITY_EMAIL, opportunity.getContactEmail());
        values.put(OPPORTUNITY_STATUS, opportunity.getStatus());
        values.put(OPPORTUNITY_PRIORITY, opportunity.getPriority());
        values.put(OPPORTUNITY_NOTES, opportunity.getNotes());
        values.put(OPPORTUNITY_IMAGE, opportunity.getImageUri());

        return database.insert(TABLE_OPPORTUNITIES, null, values);
    }

    public ArrayList<Opportunity> getOpportunitiesForUser(long userId) {
        ArrayList<Opportunity> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    TABLE_OPPORTUNITIES,
                    null,
                    OPPORTUNITY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    OPPORTUNITY_DEADLINE + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(cursorToOpportunity(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public Opportunity getOpportunityById(long opportunityId, long userId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    TABLE_OPPORTUNITIES,
                    null,
                    OPPORTUNITY_ID + " = ? AND " + OPPORTUNITY_USER_ID + " = ?",
                    new String[]{String.valueOf(opportunityId), String.valueOf(userId)},
                    null,
                    null,
                    null,
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToOpportunity(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public boolean updateOpportunity(Opportunity opportunity) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OPPORTUNITY_TITLE, opportunity.getTitle());
        values.put(OPPORTUNITY_ORGANIZATION, opportunity.getOrganization());
        values.put(OPPORTUNITY_CATEGORY, opportunity.getCategory());
        values.put(OPPORTUNITY_DESCRIPTION, opportunity.getDescription());
        values.put(OPPORTUNITY_DEADLINE, opportunity.getDeadline());
        values.put(OPPORTUNITY_LOCATION, opportunity.getLocation());
        values.put(OPPORTUNITY_LINK, opportunity.getApplicationLink());
        values.put(OPPORTUNITY_EMAIL, opportunity.getContactEmail());
        values.put(OPPORTUNITY_STATUS, opportunity.getStatus());
        values.put(OPPORTUNITY_PRIORITY, opportunity.getPriority());
        values.put(OPPORTUNITY_NOTES, opportunity.getNotes());
        values.put(OPPORTUNITY_IMAGE, opportunity.getImageUri());

        int rows = database.update(
                TABLE_OPPORTUNITIES,
                values,
                OPPORTUNITY_ID + " = ? AND " + OPPORTUNITY_USER_ID + " = ?",
                new String[]{String.valueOf(opportunity.getId()), String.valueOf(opportunity.getUserId())}
        );
        return rows > 0;
    }

    public boolean deleteOpportunity(long opportunityId, long userId) {
        SQLiteDatabase database = getWritableDatabase();
        int rows = database.delete(
                TABLE_OPPORTUNITIES,
                OPPORTUNITY_ID + " = ? AND " + OPPORTUNITY_USER_ID + " = ?",
                new String[]{String.valueOf(opportunityId), String.valueOf(userId)}
        );
        return rows > 0;
    }

    public int getTotalOpportunityCount(long userId) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_OPPORTUNITIES + " WHERE " + OPPORTUNITY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public int getOpportunityCountByStatus(long userId, String status) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_OPPORTUNITIES + " WHERE " + OPPORTUNITY_USER_ID + " = ? AND " + OPPORTUNITY_STATUS + " = ?",
                    new String[]{String.valueOf(userId), status}
            );
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public boolean updateUserPassword(long userId, String newPasswordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(USER_PASSWORD_HASH, newPasswordHash);
        int rows = db.update(TABLE_USERS, values, USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }
}
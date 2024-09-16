package mega.privacy.android.data.database

/**
 * Database constant
 * Share constant between legacy SQLite and new Room database
 */
object MegaDatabaseConstant {
    /**
     * Database Version
     */
    const val DATABASE_VERSION = 89

    /**
     * Database Name
     */
    const val DATABASE_NAME = "megapreferences"

    /**
     * Table Contacts
     */
    const val TABLE_CONTACTS = "contacts"

    /**
     * Table Completed Transfers
     */
    const val TABLE_COMPLETED_TRANSFERS = "completedtransfers_2"

    /**
     * Table Completed Transfers legacy
     */
    const val TABLE_COMPLETED_TRANSFERS_LEGACY = "completedtransfers"

    /**
     * Table Active Transfers
     */
    const val TABLE_ACTIVE_TRANSFERS = "active_transfers"

    /**
     * Table Sd Transfers
     */
    const val TABLE_SD_TRANSFERS = "sdtransfers"

    /**
     * Table Backups
     */
    const val TABLE_BACKUPS = "backups"

    /**
     * Table Camera Uploads Records
     */
    const val TABLE_CAMERA_UPLOADS_RECORDS = "camerauploadsrecords"

    /**
     * Table Offline
     */
    const val TABLE_OFFLINE = "offline"

    /**
     * Table For Android Sync solved issues list
     */
    const val TABLE_SYNC_SOLVED_ISSUES = "syncsolvedissues"

    /**
     * Table For Android Sync paused syncs list
     */
    const val TABLE_USER_PAUSED_SYNCS = "userpausedsyncs"

    /**
     * Table Chat Room Perference
     */
    const val TABLE_CHAT_ROOM_PREFERENCE = "chatroompreference"

    /**
     * Passphrase File Name
     */
    const val PASSPHRASE_FILE_NAME = "passphrase.bin"
}

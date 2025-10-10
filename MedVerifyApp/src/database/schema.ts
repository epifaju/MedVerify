/**
 * Schéma de base de données SQLite pour cache offline
 */

import SQLite from 'react-native-sqlite-storage';

SQLite.DEBUG(false);
SQLite.enablePromise(true);

const DATABASE_NAME = 'medverify.db';
const DATABASE_VERSION = 1;

let database: SQLite.SQLiteDatabase | null = null;

/**
 * Initialise la base de données
 */
export const initializeDatabase = async (): Promise<void> => {
  try {
    database = await SQLite.openDatabase({
      name: DATABASE_NAME,
      location: 'default',
    });

    await createTables();
    console.log('Database initialized successfully');
  } catch (error) {
    console.error('Failed to initialize database:', error);
    throw error;
  }
};

/**
 * Crée les tables
 */
const createTables = async (): Promise<void> => {
  if (!database) {
    throw new Error('Database not initialized');
  }

  // Table pour cache des médicaments
  await database.executeSql(`
    CREATE TABLE IF NOT EXISTS cached_medications (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      gtin TEXT UNIQUE NOT NULL,
      name TEXT NOT NULL,
      manufacturer TEXT,
      dosage TEXT,
      is_authentic BOOLEAN,
      data TEXT,
      cached_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      expires_at DATETIME
    );
  `);

  await database.executeSql(`
    CREATE INDEX IF NOT EXISTS idx_gtin ON cached_medications(gtin);
  `);

  await database.executeSql(`
    CREATE INDEX IF NOT EXISTS idx_expires ON cached_medications(expires_at);
  `);
};

/**
 * Cache un médicament
 */
export const cacheMedication = async (
  gtin: string,
  name: string,
  manufacturer: string,
  dosage: string,
  isAuthentic: boolean,
  data: any,
  ttl: number = 86400000 // 24h par défaut
): Promise<void> => {
  if (!database) {
    throw new Error('Database not initialized');
  }

  const expiresAt = new Date(Date.now() + ttl).toISOString();

  await database.executeSql(
    `INSERT OR REPLACE INTO cached_medications 
     (gtin, name, manufacturer, dosage, is_authentic, data, cached_at, expires_at)
     VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)`,
    [gtin, name, manufacturer, dosage, isAuthentic ? 1 : 0, JSON.stringify(data), expiresAt]
  );
};

/**
 * Récupère un médicament en cache
 */
export const getCachedMedication = async (gtin: string): Promise<any | null> => {
  if (!database) {
    throw new Error('Database not initialized');
  }

  const results = await database.executeSql(
    `SELECT * FROM cached_medications WHERE gtin = ? AND expires_at > CURRENT_TIMESTAMP`,
    [gtin]
  );

  if (results[0].rows.length === 0) {
    return null;
  }

  const row = results[0].rows.item(0);
  return {
    ...row,
    data: JSON.parse(row.data),
    is_authentic: Boolean(row.is_authentic),
  };
};

/**
 * Nettoie les entrées expirées du cache
 */
export const clearExpiredCache = async (): Promise<void> => {
  if (!database) {
    throw new Error('Database not initialized');
  }

  await database.executeSql(
    `DELETE FROM cached_medications WHERE expires_at < CURRENT_TIMESTAMP`
  );
};

/**
 * Ferme la base de données
 */
export const closeDatabase = async (): Promise<void> => {
  if (database) {
    await database.close();
    database = null;
  }
};

export default {
  initializeDatabase,
  cacheMedication,
  getCachedMedication,
  clearExpiredCache,
  closeDatabase,
};



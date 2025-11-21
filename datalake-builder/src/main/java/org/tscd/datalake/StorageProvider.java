package org.tscd.datalake;

public interface StorageProvider {

   void save(String filePath);
   void createStorage();
}

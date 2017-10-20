gcc `mysql_config --cflags` `mysql_config --libs` -O pcbo.c -lparapin -lpthread -o para
./para

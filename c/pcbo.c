/*

  This code is used to transfer the digital data of an ADC804 connected to the parallel port of the computer to a MySQL database. 

  The pins of the parallel port are connected as follows:
  
  01: NC
  02: D0 (18)
  03: D1 (17)
  04: D2 (16)
  05: D3 (15)
  06: D4 (14)
  07: D5 (13)
  08: D6 (12)
  09: D7 (11)
  10: INTR (5)
  11: NC
  12: NC
  13: NC
  14: NC
  15: NC
  16: RD (2)
  17: WR (3)
  18: GND
  19: GND
  20: GND
  21: GND
  22: GND
  23: GND
  24: GND
  25: GND

  These pins are connected through a bidirectional, octal bus transciever (buffer), SN74LS245

  The mysql.h library and parapin.h library must be successfully installed on the system.

  The code uses the parallel port address of ADD, and inserts NUM*I entries into the database, with a I inserts per query.

*/

#include<mysql.h>
#include<stdio.h>
#include<stdlib.h>
#include"parapin.h"
#include"pthread.h"

/**
* Function to generate alternate threads
*/
void *query_function0();

/**
* Function to generate alternate threads
*/
void *query_function1();

char *ctrl = " UPDATE `CONTROL` SET `VALUE` = `VALUE` + 1 WHERE `PARAMETER` = 'c_row'; \0";

MYSQL *conn;
MYSQL_RES *res;
MYSQL_ROW row;
  
char *server = "127.0.0.1";//enter the ip address of the server hosting the MySQL database here
char *user = "PCBOuser";
char *password = "PCBOpassword";
char *database = "PCBO";
char *final[2];

main()
{

  //Variable declaration

  char q1[10] , *str1 , *str2 , *str3 , *str4 , temp1[10] , temp2[10];
  int a[1000] , i , j , k , l , num = 0 , data , I , NUM , ADD, b, swit, iret0, iret1;
  long long int del1 , del2;

  pthread_t mysqlt0,mysqlt1;

  char *init1 = " UPDATE `CONTROL` SET `VALUE` = 0 WHERE `PARAMETER` = 'c_row'; \0";
  char *init2 = " TRUNCATE TABLE `DATA`; \0";

  //Components of query string
 
  str1 = ( char* )malloc( 53 * sizeof( char ) );
  str2 = ( char* )malloc( 5 * sizeof( char ) );
  str3 = ( char* )malloc( 4 * sizeof( char ) );
  str1 = "INSERT INTO `DATA` ( `data` ) VALUES \0";
  str2 = "( '\0";
  str4 = "' ), \0";

  //Set parameters here

  NUM = 100;
  I = 1000;
  ADD = 0x378;
 
  //Initalise parallel port

  pin_init_user( ADD );                       //Pass parallel port address
  pin_input_mode( LP_DATA_PINS | LP_PIN10 );  //Set data bus, pin10 in input mode
  pin_output_mode( LP_PIN17 | LP_PIN16 );     //Set pins 16, 17 as output mode
 
  //Connect to MySQL server

  conn = mysql_init(NULL);

  if ( !mysql_real_connect( conn, server, user, password, database, 0, NULL, 0 ) ) 
    {

      fprintf( stderr , "%s\n", mysql_error( conn ) );
      exit(0);

    }
  
  if ( mysql_query( conn , init1 ) ) 
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    } 
  if ( mysql_query( conn , init2 ) ) 
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    }  

  //Main coding
  
  //Main Thread

  for(i=0;i<2;i++)
    {
      final[i]=( char* )malloc( 14200 * sizeof( char ) );
    }
  
  for( l = 0 ; *(str1+l) != '\0' ; l++ )
    {
      *(final[0] + l) = *(str1 + l);
      *(final[1] + l) = *(str1 + l);
    }

  for( num = 0 ; num < 25000 ; num++ )

    {

      k = l;
      swit = num % 2;
      for( i = 0 ; i < 400 ; i++ )

	{

	  //WR transits from 1 to 0 to start conversion in ADC

	  clear_pin( LP_PIN17 );

	  //Delay for buffer
	  
	  for(del1=0;del1<10;del1++)
	    for(del2=0;del2<20;del2++)
	      nanosleep(100);

	  //WR and RD transit from 0 to 1
  
	  set_pin( LP_PIN17 | LP_PIN16 );
  
	  //Delay for buffer
	  
	  for(del1=0;del1<10;del1++)
	    for(del2=0;del2<10;del2++)
	      nanosleep(100);	 

	  //Poll for interrupt to transit from 1 to 0 indicating end of conversion

	  while( pin_is_set( LP_PIN10 ) );

	  //RD transits from 1 to 0 to get data from internal register of ADC to the data bus

	  clear_pin( LP_PIN16 );
	  
	  //The data variable gives the digital output of the ADC

	  data = pin_is_set( LP_PIN09 )/128 + pin_is_set( LP_PIN08 )/32 + pin_is_set( LP_PIN07 )/8 + pin_is_set( LP_PIN06 )/2 + pin_is_set( LP_PIN05 )*2 + pin_is_set( LP_PIN04 )*8 + pin_is_set( LP_PIN03 )*32 + pin_is_set( LP_PIN02 )*128 - 128;
	  
	  sprintf( temp2 , "%d" , data );

	  for(j = 0 ; *(str2+j) != '\0' ; j++)
	    *(final[swit]+k+j) = *(str2+j);

	  k += j;

	  for( j = 0 ; *(temp2 + j) != '\0' ; j++)
	    *(final[swit] + k + j) = *(temp2 + j);

	  k += j;

	  for( j = 0 ; *(str4 + j) != '\0' ; j++)
	    *(final[swit] + k + j) = *(str4 + j);

	  k += j;

	  nanosleep(1000);

	}

      *(final[swit] + k + j - 7) = ';';
      
      *(final[swit] + k + j - 6) = '\0';
      
      if(!num)
	{

	  iret0 = pthread_create( &mysqlt0, NULL, query_function0,NULL);

	}

      else if (num==1)
	{

	  iret1 = pthread_create( &mysqlt1, NULL, query_function1,NULL);

	}
      else
	{
	
	  if(swit)
	    {
	
	      pthread_join(mysqlt1,NULL);
	      iret1 = pthread_create( &mysqlt1, NULL, query_function1,NULL);
	    
	    }
	  else
	    {
	
	      pthread_join(mysqlt0,NULL);
	      iret0 = pthread_create( &mysqlt0, NULL, query_function0,NULL);
	 
	    }
	
	}

    }
 
  //Close MySQL connection

  mysql_close( conn );
  
  pthread_join(mysqlt0,NULL);
  pthread_join(mysqlt1,NULL);

  return 0;
  
}  


void *query_function0()
{
 
  if ( mysql_query( conn , final[0] ) ) 
    
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    }   
  
  if ( mysql_query( conn , ctrl ) ) 
    
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    }
 
}

void *query_function1()
{
 
  if ( mysql_query( conn , final[1] ) ) 
    
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    }   
  
  if ( mysql_query( conn , ctrl ) ) 
    
    {
      
      fprintf( stderr , "%s\n" , mysql_error( conn ) );
      exit( 1 );
      
    }
 
}

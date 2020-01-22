package coffee;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;



/* ------------create a random string for trans_id---------------- */
class RandomUtil {
    private String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public String generateStr(int len){
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i <len ; i++) {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return  sb.toString();
 }
}

public class Coffee {
	
	public static String userid;
	static Date date_in = new Date();      
	static Timestamp timeStamep_in = new Timestamp(date_in.getTime());
	
	public static Connection getConn() {
		String driver = "org.postgresql.Driver";
		Connection conn = null;
		try {
			Class.forName(driver); // classLoader,加载对应驱动
			conn = (Connection) DriverManager.getConnection("jdbc:postgresql://localhost:5432/janeliu", "janeliu", "2018");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/* -----------------login--------------- */
	public static boolean login(Scanner in) {		
		System.out.println("Please input your user_id:");
		userid = in.nextLine();	
		System.out.println("Please input your password:");
		String password = in.nextLine();
		Connection c = getConn();		
	    String sql = "SELECT user_id, password FROM user_tbl where user_id ='" + userid + "';";
	    try {
	    	Statement stmt = c.createStatement();
	    	ResultSet rs = stmt.executeQuery(sql);	
	    	if(!rs.next()) {
	    		System.out.println("This user does not exist, please re-enter!!!!!!!");
	    		return false;
	    	} else {
	    		if(!rs.getString("password").equals(password))
	    		{
	    			System.out.println("Your password is wrong, Please input again!!!!!!!");
	    			return false;
	    		}
	    	}
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return true;
	}
	
	/* ----------------menu---------------- */
	public static void menu(Scanner in) {
		//login timestamp
		Date date = new Date();      
		Timestamp timeStamep = new Timestamp(date.getTime());
		System.out.println(timeStamep);
		
	    //initial menu
		System.out.println(  "Welcome to the coffee store!!!"+"\n"
			    +"=================================================="+"\n"
			    +"Please choose what action do you want:"+"\n"
				+ "1.Registe a sale transaction."+"\n"
				+ "2.Create new customer."+"\n"
				+ "3.Access the business reporting."+"\n"
				+ "4.Create a new employee user account."+"\n"
				+ "5.Logout."+"\n"
				+"Your input is: ");
			int op = in.nextInt();
			switch (op) {	
			case 1:
				while(!transaction(in));
				menu(in);
				break;
			case 2:
				customer(in);
				break;
			case 3:
				reporter(in);	
				break;
			case 4:
				create_user(in);		
				break;
			case 5:
				//logout timestamp + insert to session table
				Date date_out = new Date();      
				Timestamp timeStamep_out = new Timestamp(date_out.getTime());
				System.out.println(timeStamep_out);
				Connection c = getConn();
				PreparedStatement pstmt;
				String sql = "INSERT INTO session_tbl(user_id,login_time,logout_time) VALUES(?,?,?)";		
				try { 
					pstmt = (PreparedStatement) c.prepareStatement(sql);
			        pstmt.setString(1, userid);
			        pstmt.setTimestamp(2, timeStamep_in);
			        pstmt.setTimestamp(3, timeStamep_out); 
			        pstmt.executeUpdate();
			        pstmt.close();
			        c.close();
			    } catch (SQLException e) {
			        e.printStackTrace();
			    }
				System.out.println("********************"+ userid + " Logout successful!!!!!!!!!!"+"*******************");
				System.exit(0);		
				break;
			default:
				break;
			}
	}
	
	/* ------------transaction------------- */
	public static boolean transaction( Scanner in) {
		RandomUtil random = new RandomUtil();
		String trans_id = random.generateStr(10);
		System.out.println("Please input customer's id:");
		in.nextLine();
		String cus_id = in.nextLine();
		Connection c = getConn();
		Statement stmt = null;
		try {
			stmt = c.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//get reword points of customer
		String sql = "SELECT available_point FROM customer_tbl where customer_id ='" + cus_id + "';";
	    try {
	    	
	    	ResultSet rs = stmt.executeQuery(sql);	
	    	if(!rs.next()) {
	    		System.out.println("This customer does not exist, please re-enter!!!!!!!");
	    		return false;
	    	}else{
	    		int point = rs.getInt("available_point");
	    		if(point >100) 
	    		{ 
		    		System.out.println("This customer have **"+ point +"** points!!!!!!"+"\n"
		    							+"Do you want to use 100 points to get a reword?"+"\n"
		    							+"1.Yes"+"\n"
		    							+"2.No");
		    		int i = in.nextInt();
		    		switch (i) {
					case 1:
						String sql1 = "UPDATE customer_tbl SET available_point= available_point-100 WHERE customer_id ='"+cus_id+"';"; 
					    stmt.executeUpdate(sql1);	
					    System.out.println("Now this customer has **"+(point-100)+"** points!!!!!!");
						break;
					case 2:					
						break;
					default:
						break;
					}
	    		
	    		}
	    	}
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }	
	    
	    //customer order
	    boolean re = true;
	    int pro_1 = 0;
	    int pro_2 = 0;
	    int pro_3 = 0;
	    while(re)
	    {
		    System.out.println("Which products do you want to order:"+"\n"
		    					+"1.Beverages  $4.5"+"\n"
		    					+"2.Donuts     $3.5"+"\n"
		    					+"3.Bagels     $3");
		    int pro = in.nextInt();   
		    
		    System.out.println("How many do you want to order: ");
		    int pro_num = in.nextInt(); 
		    switch (pro) {
			case 1:
				pro_1 = pro_1+pro_num;
				break;
			case 2:
				pro_2 = pro_2+pro_num;
				break;
			case 3:
				pro_3 = pro_3+pro_num;
				break;
			default:
				break;
			}
		    System.out.println("Do you want some else?"+"\n"
		    					+"1.Yes"+"\n"
		    					+"2.No");
		    in.nextLine();
		    String j = in.nextLine();
		    if(j.equals("2"))
		    	re = false;
		}
	    
	    //total price
	    float tot_price = 0;
	    if(pro_1>0)//sale Beverages
	    {
	    	tot_price = (float) (tot_price + 4.5 * pro_1);    	
	    }	    
	    if(pro_2>0)//Donuts
	    {
	    	tot_price = (float) (tot_price + 3.5 * pro_2);
	    }	    
	    if(pro_3>0)//Bagels
	    {
	    	tot_price = (float) (tot_price + 3 * pro_3);
	    }
	    
	    //add transaction record
	    Date date = new Date();
	    Timestamp timeStamep = new Timestamp(date.getTime());
	    String sql_trans = "INSERT INTO transaction_tbl(transaction_id,customer_id,price,time) VALUES (?,?,?,?);";
	    PreparedStatement pstmt;
		try { 
			pstmt = (PreparedStatement) c.prepareStatement(sql_trans);
	        pstmt.setString(1, trans_id);  
	        pstmt.setString(2, cus_id); 
	        pstmt.setFloat(3, tot_price);
	        pstmt.setTimestamp(4, timeStamep);
	        pstmt.executeUpdate();
	        System.out.println("Add transaction successfully!!!!!!!!!!!");
	        pstmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		
		//add reword points for customer
		String sql_point = "UPDATE customer_tbl SET available_point = available_point+"+tot_price+" WHERE customer_id ='"+cus_id+"';";
    	try {
 	    	stmt.executeUpdate(sql_point);
 	    } catch (SQLException e) {
 	        e.printStackTrace();
 	    }
		
		//add sale record		
		if(pro_1>0)//Beverages
	    {
	    	String sql_pro1 = "INSERT INTO sale_tbl(transaction_id,product_id,quantity) VALUES (?,?,?);";
	    	PreparedStatement pstmt1;
			try { 
				pstmt1 = (PreparedStatement) c.prepareStatement(sql_pro1);
		        pstmt1.setString(1, trans_id);  
		        pstmt1.setString(2, "001"); 
		        pstmt1.setInt(3, pro_1);
		        pstmt1.executeUpdate();
		        System.out.println("Add Beverages sale successfully!!!!!!!!!!!");
		        pstmt1.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
			
	    	String sql_stock1 = "UPDATE product_tbl SET number_in_stock = number_in_stock-"+pro_1+" WHERE product_id ='001';";
	    	try {
	 	    	stmt.executeUpdate(sql_stock1);
	 	    } catch (SQLException e) {
	 	        e.printStackTrace();
	 	    }
	    }	    
		if(pro_2>0)//Donuts
	    {
	    	String sql_pro2 = "INSERT INTO sale_tbl(transaction_id,product_id,quantity) VALUES (?,?,?);";
	    	PreparedStatement pstmt2;
			try { 
				pstmt2 = (PreparedStatement) c.prepareStatement(sql_pro2);
		        pstmt2.setString(1, trans_id);  
		        pstmt2.setString(2, "002"); 
		        pstmt2.setInt(3, pro_2);
		        pstmt2.executeUpdate();
		        System.out.println("Add Donuts sale successfully!!!!!!!!!!!");
		        pstmt2.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
			
	    	String sql_stock2 = "UPDATE product_tbl SET number_in_stock = number_in_stock-"+pro_2+" WHERE product_id ='002';";
	    	try {
	 	    	stmt.executeUpdate(sql_stock2);
	 	    } catch (SQLException e) {
	 	        e.printStackTrace();
	 	    }
	    }	    
	    if(pro_3>0)//Bagels
	    {
	    	String sql_pro3 = "INSERT INTO sale_tbl(transaction_id,product_id,quantity) VALUES (?,?,?);";
	    	PreparedStatement pstmt3;
			try { 
				pstmt3 = (PreparedStatement) c.prepareStatement(sql_pro3);
		        pstmt3.setString(1, trans_id);  
		        pstmt3.setString(2, "003"); 
		        pstmt3.setInt(3, pro_3);
		        pstmt3.executeUpdate();
		        System.out.println("Add Bagels sale successfully!!!!!!!!!!!");
		        pstmt3.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
	    	String sql_stock3 = "UPDATE product_tbl SET number_in_stock = number_in_stock-"+pro_3+" WHERE product_id ='003';";
	    	try {
	 	    	stmt.executeUpdate(sql_stock3);
	 	    } catch (SQLException e) {
	 	        e.printStackTrace();
	 	    }
	    }
	    try {
			stmt.close();
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    
		return true;
	}
	
	/* -----------------add customer------------------ */
	public static void customer(Scanner in) {
		System.out.println("Please input the customer's information,(use following sequence, and split by \",\"):"+"\n"
							+"customer_id, first_name, last_name, Street, city, state" );
		in.nextLine();
		String customer = in.nextLine();
		String[] info = customer.split(",");
		Connection c = getConn();
		PreparedStatement pstmt;
		String sql = "INSERT INTO customer_tbl(customer_id,first_name,last_name,street,city,state, available_point) VALUES(?,?,?,?,?,?,?)";		
		int i = 0;
		try { 
			pstmt = (PreparedStatement) c.prepareStatement(sql);
	        pstmt.setString(1, info[0]);
	        pstmt.setString(2, info[1]);
	        pstmt.setString(3, info[2]); 
	        pstmt.setString(4, info[3]);
	        pstmt.setString(5, info[4]);
	        pstmt.setString(6, info[5]);
	        pstmt.setInt(7, 0);
	        i = pstmt.executeUpdate();
	        System.out.println("Add "+i+" customer successfully!!!!!!!!!!!");
	        pstmt.close();
	        c.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    menu(in);
	}
	
	/* -----------------access report----------------- */
	public static void reporter(Scanner in) {
		System.out.println("What do you want to do:"+"\n"
				+"================================================" +"\n"
				+"1.Inventory queries" +"\n"
				+"2.Gross sales for a certain period of time"+"\n"
				+"Your choice?");
		int i = in.nextInt();
		Connection c = getConn();
		Statement stmt = null;
		try {
			stmt = c.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		switch (i) {
		//Inventory queries
		case 1:	
			
		    String sql = "SELECT product_id, name, number_in_stock FROM product_tbl;";
		    try {    	
		    	ResultSet rs1 = stmt.executeQuery(sql);	
		    	System.out.println("ID       NAME         STOCK");
		    	while(rs1.next()) 
		    	{
		    		System.out.println(rs1.getString("product_id")+"      "+rs1.getString("name")+"         "+rs1.getInt("number_in_stock"));	    		
		    	}
		    	rs1.close();
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }			
			break;
			//Gross sales for a certain period of time
		case 2:
			System.out.println("Please input the start time:(yyyy-MM-dd HH:mm:ss)");
			in.nextLine();
			String time_start = in.nextLine();
			Timestamp st_start = Timestamp.valueOf(time_start);
			System.out.println("Please input the end time:(yyyy-MM-dd HH:mm:ss)");
			String time_end = in.nextLine();
			Timestamp st_end = Timestamp.valueOf(time_end);
			 String sql1 = "SELECT SUM(price) as price FROM transaction_tbl WHERE time>'" + st_start+"' AND time<'"+st_end+"';";
			 System.out.println(sql1);
			    try {    	
			    	ResultSet rs1 = stmt.executeQuery(sql1);
			    	if(rs1.next())
			    		System.out.println("The sum is :           $"+rs1.getFloat("price"));
			    	else
			    		System.out.println("There are no sales during this period of time!!!!!!!");
			    	rs1.close();
			    } catch (SQLException e) {
			        e.printStackTrace();
			    }					
			break;

		default:
			break;
		}
		menu(in);
		
	}
	
	/* ----------------create user-------------------- */
	public static void create_user( Scanner in) {
		System.out.println("Please input the employee's information,(use following sequence, and split by \",\"):"+"\n"
						+"user_id, first_name, last_name, privilege, password" +"\n"
						+"Be careful!!!!!! privilege must be manager or employee!!!!!!!!!");
		in.nextLine();
		String customer = in.nextLine();
		String[] info = customer.split(",");
		Connection c = getConn();
		PreparedStatement pstmt;
		String sql = "INSERT INTO user_tbl(user_id, first_name, last_name, privilege, password) VALUES(?,?,?,?,?)";
		
		int i = 0;
		try { 
			pstmt = (PreparedStatement) c.prepareStatement(sql);
	        pstmt.setString(1, info[0]);
	        pstmt.setString(2, info[1]);
	        pstmt.setString(3, info[2]); 
	        if(info[3].equals("employee")||info[3].equals("manager"))
	        	pstmt.setString(4,info[3]);
	        else
	        	pstmt.setString(4,null);
	        pstmt.setString(5, info[4]);
	        i = pstmt.executeUpdate();
	        System.out.println("Add "+i+" employee successfully!!!!!!!!!!!");
	        pstmt.close();
	        c.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		menu(in);
	}
	
	public static void main(String args[]) {
			Connection c = null;
			Statement stmt = null;
			try {
				Class.forName("org.postgresql.Driver");
				c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/janeliu", "janeliu", "2018");
				c.setAutoCommit(false);
				System.out.println("Opened database successfully");	 
				stmt = c.createStatement(); 
/*		        String sql =  "CREATE TABLE user_tbl ( user_id  VARCHAR(20) PRIMARY KEY, first_name VARCHAR(20), last_name  VARCHAR(20), privilege  VARCHAR(10), password VARCHAR(20));" + 
		                     "CREATE TABLE customer_tbl ( customer_id  VARCHAR(20) PRIMARY KEY, first_name VARCHAR(10), last_name  VARCHAR(10), Street VARCHAR(100),city VARCHAR(20), state VARCHAR(20), available_point INT); " + 
		                     "CREATE TABLE product_tbl ( product_id  VARCHAR(20) PRIMARY KEY,name VARCHAR(20), price FLOAT, number_in_stock INT);" +
		                     "CREATE TABLE transaction_tbl ( transaction_id  VARCHAR(20) PRIMARY KEY, customer_id VARCHAR(20),price FLOAT, time TIMESTAMP, FOREIGN KEY(customer_id) REFERENCES customer_tbl(customer_id));" +
		                     "CREATE TABLE session_tbl (user_id VARCHAR(20), login_time  TIMESTAMP,logout_time TIMESTAMP, PRIMARY KEY (login_time,logout_time,user_id),FOREIGN KEY (user_id) REFERENCES user_tbl(user_id));" +
		                     "CREATE TABLE sale_tbl (transaction_id VARCHAR(20), product_id VARCHAR(20),quantity INT, PRIMARY KEY (product_id, transaction_id),FOREIGN KEY (product_id) REFERENCES product_tbl(product_id),FOREIGN KEY (transaction_id) REFERENCES transaction_tbl(transaction_id));";
		        stmt.executeUpdate(sql); 
		        System.out.println("Table created successfully");
*/	 		         
				stmt.close();
				c.commit();
				c.close();
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
					    
		    Scanner in = new Scanner(System.in);
		    while(!login(in));
			menu(in);
			
			in.close();	
		}
}

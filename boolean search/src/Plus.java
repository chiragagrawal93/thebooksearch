
import stopwordsremoval.*;
import java.util.*;
import java.sql.*;
import java.io.FileReader;
import java.io.BufferedReader;
import org.tartarus.snowball.ext.*;
public class Plus {
	
  public  static Vector<String> tokens = new Vector();
  public static Connection connection = null;
  public static Statement stmt = null; 

   public static void main(String args[])
	{
		String query = new String();
		Stopwords s = new Stopwords();
		double score =0.0;
		double idf;
		double page_count=0;
		double numberOfPages=0;
		int page_no =0;
		Vector<Integer> page = new Vector(); 
		Vector<Integer>currentPage = new Vector();
		Vector<String>required_terms  = new Vector();
		Vector<Double>inv_freq_tokens = new Vector();
		Vector<Double>pageScore = new Vector();
//		Vector<String>headingPage = new Vector();
		Vector<Double>inv_freq_required_terms = new Vector();
		Vector<Integer>head_doc_id = new Vector();
		Vector<Integer>head_length = new Vector();
		int frequency;
		englishStemmer stemmer = new englishStemmer();
		 String book_title = new String();
		    book_title = "Computer Networks";
		 
		 
		Scanner input = new Scanner(System.in);
		query = input.nextLine();
		String[] root = query.split("\\s+");
		for(int i=0;i<root.length;i++)
		{
			
			char[] ch = root[i].toCharArray();
			if(ch[0] == '+')
			{
				  stemmer.setCurrent(root[i]);
				  stemmer.stem();
				  root[i] = stemmer.getCurrent();
				  required_terms.addElement(root[i].toLowerCase());
				  break;
			}
			if(!s.m_Words.contains(root[i].toLowerCase()))
			{
				  stemmer.setCurrent(root[i]);
				  stemmer.stem();
				  root[i] = stemmer.getCurrent();
				  tokens.addElement(root[i]);
			}
		}

						
		try 
		 {
			Class.forName("com.mysql.jdbc.Driver");
		 } 
		catch (ClassNotFoundException e) 
		 {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		 }
	 
		System.out.println("MySQL JDBC Driver Registered!");
		
		Connection connection = null;
		Statement stmt = null;
	   try 
		{
			connection = DriverManager
			.getConnection("jdbc:mysql://localhost:3306/project","root", "");
	 
		} 
		catch (SQLException e)
		{
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
	 
		if (connection != null)
		{
			System.out.println("You made it, take control your database now!");
		} 
		else
		{
			System.out.println("Failed to make connection!");
		}
				
		
		try
		{
			
		
			for(int j=0;j<tokens.size();j++)
			{	  
			   String sql_numberOfPages = "SELECT sum(no_of_pages) as sum FROM books";
			   ResultSet rs_numberOfPages = stmt.executeQuery(sql_numberOfPages);
			   
			   while(rs_numberOfPages.next())
				 numberOfPages = rs_numberOfPages.getDouble(1);
			   
		
			   String sql_pageCount = "SELECT SUM(page_count) as sum FROM "+
					                  "revindex WHERE "+
					                  "term= " + "'" + tokens.elementAt(j); 
			   if(stmt.equals(null))
				   System.out.println("null");
			   ResultSet rs_pageCount = stmt.executeQuery(sql_pageCount);
			   while(rs_pageCount.next())
				    page_count = rs_pageCount.getDouble(1);
			   idf = Math.log(numberOfPages/page_count);
			   inv_freq_tokens.addElement(idf);  
			}
			
			for(int j=0;j<required_terms.size();j++)
			{	  
			   
			   String sql_pageCount = "SELECT SUM(page_count) as sum FROM "+
					                  "revindex WHERE "+
					                  "term= " + "'" + required_terms.elementAt(j) + "'"; 
			   ResultSet rs_pageCount = stmt.executeQuery(sql_pageCount);
			   while(rs_pageCount.next())
				    page_count = rs_pageCount.getDouble(1);
			   idf = Math.log(numberOfPages/page_count);
			   inv_freq_required_terms.addElement(idf);  
			}
			
			int books;
			String sql_pages = " ";
		    String sub_query = " ";
			for(int j=0;j<required_terms.size();j++)
			{	
				
				page.clear();
				if(j!=0)
					sub_query = "UNION ";
				
		    	sql_pages = sub_query + "SELECT doc_id FROM" +
			             " indexing  WHERE " + 
					     "term =" + "'" + required_terms.elementAt(j) + "'";
			}
		
			
			
			
			{
				
			   ResultSet rs = stmt.executeQuery(sql_pages);
	//		   idf = rs.getInt("idf");
			   while(rs.next())
			   {	   page_no = rs.getInt("doc_id");
			           page.addElement(page_no);
			   }
			   
			
			   for(int i=0;i<page.size();i++)
			   {  
				  score =0;
				  frequency=0;
				   for(int j=0;j<inv_freq_tokens.size();j++)
				   {
					   String search_tokens = "SELECT frequency from INDEXING "+
							   				  "WHERE term= " + "'" + tokens.elementAt(j) + "'"+
							                   "AND doc_id = " + page.elementAt(i);
					   ResultSet tokens_score = stmt.executeQuery(search_tokens);
					   frequency = tokens_score.getInt("frequency");
					   score = score + frequency*inv_freq_tokens.elementAt(j); 
				   }
				   for(int j=0;j<inv_freq_required_terms.size();j++)
				   {
					   String search_required_terms = "SELECT frequency from INDEXING "+
							   				  "WHERE term= " + "'" + required_terms.elementAt(j) + "'"+
							                   "AND doc_id = " + page.elementAt(i);
					   ResultSet required_terms_score = stmt.executeQuery(search_required_terms);
					   frequency = required_terms_score.getInt("frequency");
					   score = score + frequency*inv_freq_required_terms.elementAt(j); 
				   }
				   
				   String sql3 = "INSERT INTO results " + 
	   			            "VALUES (" + score + ", " + page.elementAt(i) +")";
	                   stmt.executeUpdate(sql3);

			   }
			}
	//	}
	}
		catch(SQLException se)
		 {
			se.printStackTrace();
		  }
	    catch(Exception e)
	      {
	        e.printStackTrace();
	      }
				
//				for(int i=0;i<pageScore.size()-1;i++)
//				for(int j=i+1;j<pageScore.size();j++)
//				{
//						if(pageScore.elementAt(i)<pageScore.elementAt(j))
//					{
//							Collections.swap(pageScore, i, j);
//							Collections.swap(currentPage, i, j);
//							Collections.swap(book_titles, i, j);
//						}
//				}
		
		 int headingPage;
		 int length;
		
		 for(int i=0;i<tokens.size();i++)
		 { 
			 String sql6 = "SELECT length,doc_id FROM titles WHERE heading like '%" + required_terms.elementAt(i) +"%'";
		   
		 try {
			ResultSet rs_four = stmt.executeQuery(sql6);
			while(rs_four.next())
			{
				head_doc_id.addElement(rs_four.getInt("doc_id"));
				head_length.addElement(rs_four.getInt("length"));
			}
			   for(int j=0;j<head_doc_id.size();j++)
			    {
				    headingPage= head_doc_id.elementAt(j);
				    length=head_length.elementAt(j);
				    if(currentPage.contains(headingPage))
				    {
				    	String sql7 = "SELECT score FROM results WHERE doc_id = "+ headingPage;
				    	ResultSet rs_five = stmt.executeQuery(sql7);
				    	while(rs_five.next())	
				    		score = rs_five.getDouble("score") + 1/(double)length;
				    
				    	String sql8 = "UPDATE results SET score =" + score + "WHERE doc_id=" + headingPage;
				    	stmt.executeUpdate(sql8);    	
				    }
				    else{
				    	score = (1/(double)(length));
				    	String sql9 = "INSERT INTO results "+
				                      "VALUES ("+ score + "," + headingPage + "')";
				    	
				    	stmt.executeUpdate(sql9); 
				    	currentPage.addElement(headingPage);
				    }
		    	}
		    } catch (SQLException e) {
			  e.printStackTrace();
			  
		     }
		 
		 }     
		
		
		
				System.out.println("results!!!!!!!!");
	    String sql4 = "Select d.page_no, d.book_title, r.score FROM results r,documents d " +
                      "WHERE d.doc_id = r.doc_id" +
                       " ORDER BY score DESC";
	   
	    try {
	    	ResultSet rs_three =stmt.executeQuery(sql4);
	    	
	    	while(rs_three.next())
	    	{	System.out.println(rs_three.getInt(1));
	    	//System.out.println(" " + rs_three.getDouble("score"));  
	    	
	    	}
		} catch (SQLException e) {
				e.printStackTrace();
		}
	    catch(NullPointerException e)
	    {
	    	e.printStackTrace();
	    }
	    
	    String sql5 = "TRUNCATE TABLE  results";
	    try {
		stmt.executeQuery(sql5);
		}
	     catch (SQLException e)
	    {
			e.printStackTrace();
		}
	  
	}
}
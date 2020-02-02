package project1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Stream;

public class Tokenizer {
	public static String fileReader(String fileName) {
		StringBuilder result = new StringBuilder("");
		try (Stream<String> lines = Files.lines(Paths.get(fileName))){
			lines.forEachOrdered(line -> result.append(line.toLowerCase()));
		} catch (IOException e) {
			System.out.println("unable to find file: "+fileName);
			System.exit(0);
		}
		System.out.println(result.toString().replaceAll("([ ]){2,}", " "));
		return result.toString().replaceAll("([ ]){2,}", " ");
	}
	public static void add(Map<String, Integer> map, String word) {
		if(map.containsKey(word)) {
			Integer i = map.get(word);
			map.remove(word);
			map.put(word, i+1);
		}else {
			map.put(word, 1);
		}
	}
	public static Map<String,Integer> tokenize(String content){
		String[] temp = content.split(" ");
		Map<String,Integer> result = new HashMap<>();
		for(String itr: temp){
			//check the itr is a word with only [a-z]
			if(itr.matches("([a-z]){1,}")) {
				add(result,itr);
			}else if(itr.matches("[^a-z0-9]*([a-z]\\.){2,}[^a-z0-9]*")) { // check itr is abbreviation u.s. or u.s.. or u.s.,
				String word = itr.replaceAll("[^a-z]", "");
				add(result,word);
			}else if(itr.matches("([0-9]){1,}")){ // check itr is number 1,0,000,0
				String[] hold = itr.split(",");
				for(String i:hold) {
					add(result,i);
				}
			}else { // itr is some combination of letters, numbers, and other characters
				if(itr.length()>0) {
					String hold = itr;
					String firstchar = ""+hold.charAt(0);
					String lastchar = ""+hold.charAt(hold.length()-1);
					if(firstchar.matches("[a-z0-9]")==false)
						hold = hold.substring(1, hold.length());
					if(hold.length()>1 && lastchar.matches("[a-z0-9]")==false)
						hold = hold.substring(0,hold.length()-1);
					do {
						for(int i=1;i<=hold.length();i++) {
							String s = hold.substring(0,i);
							if(s.matches("([a-z0-9]){1,}")==false && s.length()>0) {
								if(s.substring(0,s.length()-1).length()>0)
									add(result,s.substring(0,s.length()-1));
								hold =hold.substring(i,hold.length());
								break;
							}
						}
						if(hold.matches("([a-z0-9]){1,}")) {
							add(result,hold);
							hold = "";
						}
					}while(hold.length()>0);
				}
			}
		}
		return result;
	}
	public static Map<String,Integer> stopping(Map<String,Integer> tokens, String stopWordFileName){
		try (Stream<String> lines = Files.lines(Paths.get(stopWordFileName))){
			lines.forEachOrdered(line -> tokens.remove(line.toLowerCase()));
		} catch (IOException e) {
			System.out.println("unable to find file: "+stopWordFileName);
			System.exit(0);
		}
		return tokens;
	}
	public static Map<String,Integer> stemming(Map<String,Integer> tokens){
		Map<String,Integer> result = new HashMap<>();
		for(Entry<String, Integer>itr: tokens.entrySet()) {
			String stemResult = stemming2b(stemming1a(itr.getKey()));
			if(result.containsKey(stemResult)==false)
				result.put(stemResult,itr.getValue());
			else {
				int temp = result.get(stemResult);
				result.put(stemResult,temp+itr.getValue());
			}
		}
		return result;
	}
	public static String stemming1a(String token) {
			if(token.matches(".*(sses)$")) {// replace sses by ss
				token = token.substring(0,token.length()-2);
			}else if(token.matches(".*(ied)$") || token.matches(".*(ies)$")) {  // replace ied or ies by i if precceded by more than one letter, otherwise by ie
				if(token.length()>4) {
					token = token.substring(0,token.length()-3)+"i";
				}else {
					token = token.substring(0,token.length()-3)+"ie";
				}
			}else if(token.length() > 1 && token.matches(".*s$")) { // delete s if preceding word contains a vowel not before the s. 
				token = token.substring(0,token.length()-1);
				if((""+token.charAt(token.length()-1)).matches("[aeiou]")==true)
					token +="s";
			}else if(token.matches(".*(us)$")||token.matches(".*(ss)$")){ // if suffix is us or ss do nothing
				token = token;
			}
			return token;
	}
	public static String stemming2b(String token) {
		if(token.matches(".*(eedly)$")||token.matches(".*(eed)$")) { // if token ends with eedly or eed
			if(token.matches(".*(eedly)$")) {
				if(token.length()>6 && token.matches(".*[aeiou](eedly)$") == false) { // if oken ends with non vowel + eedly and token is longer that 6 char
					token = token.substring(0,token.length()-5)+"ee";
				}
			}else {
				if(token.length()>4 && token.matches(".*[aeiou](eed)$") == false) { // if oken ends with non vowel + eed and token is longer that 4 char
					token = token.substring(0,token.length()-3)+"ee";
				}
			}
		}else if( token.matches(".*(ingly)$")||token.matches(".*(ed)$")||token.matches(".*(edly)$")||token.matches(".*(ing)$")){
			if(token.matches(".*[aeiou].*(ingly)$")||token.matches(".*[aeiou].*(ed)$")||token.matches(".*[aeiou].*(edly)$")||token.matches(".*[aeiou].*(ing)$")) { 	
				// removing ingly, ed, edly, ing from token if there is a vowel before these suffixes
				if(token.matches(".*(ed)$")) {
					token = token.substring(0,token.length()-2);
				}else if(token.matches(".*(ingly)$")) {
					token = token.substring(0,token.length()-5);
				}else if(token.matches(".*(edly)$")) {
					token = token.substring(0,token.length()-4);
				}else if(token.matches(".*(ing)$")) {
					token = token.substring(0,token.length()-3);
				}
				
				if(token.matches(".*(at)$")||token.matches(".*(bl)$")||token.matches(".*(iz)$")){  // if the token ends with at, bl, iz. then add e
					token = token +"e";
				}else if(!(token.matches(".*(ll)$")||token.matches(".*(ss)$")||token.matches(".*(zz)$"))){
					// or if the token ends with double letter that is not ll, ss, zz, remove the last letter
					token = token.substring(0,token.length()-1);
				}else if(token.length()==3) { // if token is short
					token = token+"e";
				}
			}
		}
		return token;
	}
	public static void main(String arg[]) {
		
		  Scanner reader = new Scanner(System.in);
		  System.out.println("enter the file location: "); String fileName =
		  reader.nextLine();
		  System.out.println("enter the stopping word file location: "); String
		  stoppingFile = reader.nextLine(); Map<String,Integer> tokens =
		  tokenize(fileReader(fileName)); //".\\src\\tokenization-input-part-A.txt"
		  System.out.println("there is "+tokens.size()+" tokens after tokenization: "+tokens); 
		  tokens = stopping(tokens,stoppingFile); //".\\src\\stopwords.txt"
		  System.out.println("after stopping, there is "+tokens.size()+" tokens left: "+tokens); 
		  tokens = stemming(tokens);
		  System.out.println("after stemming, here is "+tokens.size()+" tokens left: " +tokens); 
		  tokens.keySet().forEach(System.out::println);
		  reader.close();
		 
		
	}
}

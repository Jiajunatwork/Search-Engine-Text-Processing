package project1;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;
public class PartB {
	public static void main(String[] args) {
		Scanner reader = new Scanner(System.in);
		
		  System.out.println("enter the file location: "); 
		  String fileName = reader.nextLine();
		  System.out.println("enter the stopping word file location: "); String
		  stoppingFile = reader.nextLine();
		 
		Map<String,Integer> tokens = Tokenizer.tokenize(Tokenizer.fileReader(fileName));  //".\\src\\tokenization-input-part-B.txt"
		System.out.println("there is "+tokens.size()+" tokens after tokenization");
		tokens = Tokenizer.stopping(tokens,stoppingFile);   //".\\src\\stopwords.txt"
		System.out.println("after stopping, there is "+tokens.size()+" tokens left");
		tokens = Tokenizer.stemming(tokens);
		System.out.println("after stemming, here is "+tokens.size()+" tokens left");
		reader.close();
		
		System.out.println("top 200 words are: ");
		Map<String, Integer> sortedMap = tokens.entrySet().stream()
                .sorted(Entry.comparingByValue())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		Object[] keys = (Object[]) (sortedMap.keySet().toArray());
		for(int i=1;i<=200;i++) {
			System.out.println(keys[keys.length-i].toString()+" , "+sortedMap.get(keys[keys.length-i].toString()));
		}
	}
}

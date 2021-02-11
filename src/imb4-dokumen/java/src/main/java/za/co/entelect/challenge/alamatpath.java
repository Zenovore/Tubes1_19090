package za.co.entelect.challenge;
import java.util.*;

public class Alamatpath{        
	Node start;
	Node end;
	Node [][] penyimpananjalan;
}


public class Node{
    int x;
	int y;
	int jarak = 9999;
	int value;
	boolean visited;
	Node Parent = null;

	Node(int x,int y){
		this.x = x;
		this.y = y;
	}
}


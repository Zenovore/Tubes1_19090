package za.co.entelect.challenge;
import java.util.*;

public class Alamatpath{        
	Node start;
	Node end;
	Node [][] penyimpananjalan;

	Alamatpath(int x, int y, int xend, int yend, int size){
		this.start = new Node(x,y);
		this.end = new Node(xend,yend);
		this.penyimpananjalan = new Node [size][size];
		for (int i = 0; i < 33; i++) {
			for (int j = 0; j < 33; j++) {
				this.penyimpananjalan[i][j] = new Node(i,j);
			}
		}
	}
}





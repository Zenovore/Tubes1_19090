package za.co.entelect.challenge;

public class Node{
    int x;
	int y;
	int jarak = Integer.MAX_VALUE;
	int value;
	boolean visited;
	Node parent = null;

	Node(int x,int y){
		this.x = x;
		this.y = y;
	}

	void ubahNilai(int value){
		this.value = value;
	}
}
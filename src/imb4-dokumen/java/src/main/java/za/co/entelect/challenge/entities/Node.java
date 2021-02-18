package za.co.entelect.challenge.entities;

public class Node{
    public int x;
	public int y;
	public int jarak = Integer.MAX_VALUE;
	public int value;
	public boolean visited;
	public Node parent = null;

	public Node(int x,int y){
		this.x = x;
		this.y = y;
	}

	public void ubahNilai(int value){
		this.value = value;
	}
}

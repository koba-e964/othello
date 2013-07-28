package kobae964_app.othello;

import java.util.Arrays;
import java.util.Scanner;

public class Board {
	long dat0,dat1;
	private Board(long dat1,long dat2){
		this.dat0=dat1;
		this.dat1=dat2;
	}
	int[][] getBoard(){
		//0->empty, 1->black, 2->white
		int[][] out=new int[8][8];
		for(int i=0;i<4;i++){
			for(int j=0;j<8;j++){
				out[i][j]=(int)((dat0>>(16*i+2*j))&3L);
			}
		}
		for(int i=4;i<8;i++){
			for(int j=0;j<8;j++){
				out[i][j]=(int)((dat1>>>(16*(i-4)+2*j))&3L);
			}
		}
		return out;
	}
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder(18*8);
		int[][] board=getBoard();
		for(int[] row:board){
			for(int elem:row){
				sb.append(elem==0?"*":(elem==1?"B":"W"));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	void merePut(int i,int j,int col){
		if(i<=3){
			int off=16*i+2*j;
			dat0&=~(3L<<off);
			dat0|=(long)col<<off;
		}
		else{
			int off=16*i+2*j-64;
			dat1&=~(3L<<off);
			dat1|=(long)col<<off;
		}
	}
	int get(int i,int j){
		if(i<=3){
			int off=16*i+2*j;
			return (int)((dat0>>off)&3L);
		}
		else{
			int off=16*i+2*j-64;
			return (int)((dat1>>off)&3L);
		}
		
	}
	static final int[][] dirs={{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1},{0,-1},{1,-1}};
	public boolean placeable(int i,int j,int col){
		if(col<=0 || col>=3){
			throw new IllegalArgumentException();
		}
		if(get(i,j)!=0)return false;
		//assert dirs.length==8;
		for(int[] dir:dirs){
			boolean res=plSub0(i, j, col, dir);
			if(res)return true;
		}
		return false;
		
	}
	boolean plSub0(int i,int j,int col,int[] dir){
		int min=Integer.MAX_VALUE;
		switch(dir[0]){
		case 1:
			min=Math.min(min, 8-i);
			break;
		case -1:
			min=Math.min(min, i+1);
			break;
		default:
		}
		switch(dir[1]){
		case 1:
			min=Math.min(min, 8-j);
			break;
		case -1:
			min=Math.min(min, j+1);
			break;
		default:
		}
		int dat[]=new int[min-1];
		for(int k=1;k<min;k++){
			dat[k-1]=get(i+dir[0]*k, j+dir[1]*k);
		}
		//System.out.println("(i,j)=("+i+","+j+") min="+min+Arrays.toString(dat));
		if(min==1||dat[0]!=3-col){
			return false;
		}
		for(int k=0;k<min-1&&dat[k]!=0;k++){
			if(dat[k]==col)return true;
		}
		return false;
	}
	public void place(int i,int j,int col){
		if(placeable(i, j, col)){
			merePut(i, j, col);
			for(int[] dir:dirs){
				plSub1(i, j, col, dir);
			}
			return;
		}
		throw new IllegalArgumentException();
	}
	void plSub1(int i,int j,int col,int[] dir){
		int min=Integer.MAX_VALUE;
		switch(dir[0]){
		case 1:
			min=Math.min(min, 8-i);
			break;
		case -1:
			min=Math.min(min, i+1);
			break;
		default:
		}
		switch(dir[1]){
		case 1:
			min=Math.min(min, 8-j);
			break;
		case -1:
			min=Math.min(min, j+1);
			break;
		default:
		}
		int k;
		for(k=1;k<min;k++){
			int c=get(i+dir[0]*k, j+dir[1]*k);
			if(c==3-col){
				merePut(i+dir[0]*k, j+dir[1]*k, col);
			}else{
				break;
			}
		}
		if(k==min || get(i+dir[0]*k,j+dir[1]*k)==0){
			for(int l=1;l<k;l++){
				merePut(i+dir[0]*l,j+dir[1]*l,3-col);
			}
		}
	}
	public void chart(int cur){
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				int col=get(i, j);
				if(col!=0)
					System.out.print(col==1?"B":"W");
				else
					System.out.print(placeable(i, j, cur)?"*":".");
			}
			System.out.println();
		}
	}
	public static Board initial(){
		Board inst=new Board(0L,0L);
		inst.merePut(3, 3, 2);
		inst.merePut(3, 4, 1);
		inst.merePut(4, 3, 1);
		inst.merePut(4, 4, 2);
		return inst;
	}
	@Override
	public int hashCode(){
		return (int)(dat0^(dat0>>32L)^dat1^(dat1>>32L));
	}
	public long hash64(){
		return dat0^(dat1<<1L);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Board inst=Board.initial();
		Scanner scan=new Scanner(System.in);
		int turn=0;
		int col=1;
		int passed=0;
		while(passed<2){
			boolean ok=false;
			for(int i=0;i<8;i++){
				for (int j = 0; j < 8; j++) {
					if(inst.placeable(i, j, col)){
						ok=true;
					}
				}
			}
			if(!ok){
				passed++;
				col=3-col;
				continue;
			}
			System.out.println("turn="+turn);
			inst.chart(col);
			int i,j;
			while(true){
				System.out.print(">");
				i=scan.nextInt();
				j=scan.nextInt();
				if(inst.placeable(i, j, col)){
					inst.place(i, j, col);
					break;
				}
			}
			passed=0;
			col=3-col;
			turn++;
		}
		int b=0,w=0;
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				switch(inst.get(i, j)){
				case 1:
					b++;
					break;
				case 2:
					w++;
					break;
				default:
					break;	
				}
			}
		}
		System.out.println("black:"+b+" white:"+w);
	}

}

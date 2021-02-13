package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;
import za.co.entelect.challenge.enums.PowerUpType;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {
    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm; 

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    private int cariParent(ArrayList<Node> sirsak){
        int i = 0;
        while(sirsak.get(i).parent != null) {
            i++;
        }
        if(sirsak.get(0).parent == null){
            return 0;
        }
        return i-1;
    }

    public Command run() {
        // Worm enemyWorm = getFirstWormInRange();
        // if (enemyWorm != null) {
        //     Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
        //     return new ShootCommand(direction);
        // }
// //
//        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
//        int cellIdx =  random.nextInt(surroundingBlocks.size());
//         Cell block = surroundingBlocks.get(cellIdx);
        
        Cell[][] map = gameState.map;
//        PrintGraph(MapstoGraph(map)); // ALGORITMA MENGUBAH PETA JADI MATRIKS
        int[][] matrixmap = mapsToGraph(map);
        Pickup nearestpowup = powerUpTerdekat(map);
        // Ambil powerup
        if(nearestpowup != null){
            // TODO: belom ke pickup
            // mungkin kalo masih full health jangan diambil
            ArrayList<Node> apel = pathFinding(matrixmap,currentWorm.position.x,
                                   currentWorm.position.y,nearestpowup.x,nearestpowup.y);

            int lennn = cariParent(apel);
            if(matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 1
                && unoccupied(apel.get(lennn).x, apel.get(lennn).y, gameState)) {
                return new MoveCommand(apel.get(lennn).x, apel.get(lennn).y);
            // tambahin kalo occupied ngapain
            } else if (matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 2) {
                return new DigCommand(apel.get(lennn).x, apel.get(lennn).y);
            }
        } else {
            // Bertahan
        }
        // PrintGraph(matrixmap);

        // System.out.println(currentWorm.position.x);
        // System.out.println(currentWorm.position.y);
        // System.out.println(apel.get(lennn).x);
        // System.out.println(apel.get(lennn).y);
        // System.out.println(nearestpowup.x);
        // System.out.println(nearestpowup.y);
        // System.out.println(matrixmap[apel.get(lennn).y][apel.get(lennn).x]);
        return new DoNothingCommand();
    }

    private void PrintGraph(int[][] nangka){
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                System.out.print(nangka[i][j]);
                if (j==32){
                    System.out.println();
                }
            }
        }
    }

    private boolean unoccupied(int x, int y, GameState gameState){
        // kalo worm nya mati, positionnya tetep kah?, kalo iya cek health==0
        int i,j;
        for(j=0; j<gameState.opponents.length; j++){
            for(i=0; i<3; i++){
                if(x == gameState.opponents[j].worms[i].position.x
                    && y == gameState.opponents[j].worms[i].position.y
                    && gameState.opponents[j].worms[i].health != 0){
                    return false;
                }
            }
        }
        for(i=0; i<3; i++){
            if(x == gameState.myPlayer.worms[i].position.x
                && y == gameState.myPlayer.worms[i].position.y
                && gameState.myPlayer.worms[i].health != 0){
                return false;
            }
        }
        return true;
    }

    private ArrayList<Node> pathFinding(int[][] peta, int x, int y, int xend, int yend){
        Alamatpath jalan = new Alamatpath(x,y,xend,yend,33);
        // INISIASI VALUE DARI PETA MATRIX PADA NODE
        for (int i = 0; i < 33; i++)
            for (int j = 0; j < 33; j++)
                jalan.penyimpananjalan[i][j].ubahNilai(peta[i][j]);

        jalan.start.jarak = 0; 
        Comparator<Node> adjacencyComparator = (left, right) -> {
            if (left.jarak > right.jarak) return 1;
            return -1;
        };

        Queue<Node> queuejalan = new PriorityQueue(33, adjacencyComparator);
        queuejalan.add(jalan.start);

        while (queuejalan.size() > 0){
            Node pinpoint = queuejalan.remove();
            // ALGORITMA UNTUK MENGECEK PERJALANAN KE 8 ARAH
            if (pinpoint.y - 1 >= 0){
                queuejalan = helper(jalan, 0, -1, pinpoint, queuejalan); // UP
                if (pinpoint.x + 1 < 33) queuejalan = helper(jalan, 1, -1, pinpoint, queuejalan); // RIGHT
                if (pinpoint.x - 1 >= 0) queuejalan = helper(jalan, -1, -1, pinpoint, queuejalan); // LEFT
            }
            if (pinpoint.y + 1 < 33){ // DOWN
                queuejalan = helper(jalan, 0, 1, pinpoint, queuejalan); // DOWN
                if (pinpoint.x + 1 < 33) queuejalan = helper(jalan, 1, 1, pinpoint, queuejalan); // RIGHT
                if (pinpoint.x - 1 >= 0) queuejalan = helper(jalan, -1, 1, pinpoint, queuejalan); // LEFT
            }
            if (pinpoint.x + 1 < 33) queuejalan = helper(jalan, 1, 0, pinpoint, queuejalan); // RIGHT
            if (pinpoint.x - 1 >= 0) queuejalan = helper(jalan, -1, 0, pinpoint, queuejalan); // LEFT
            pinpoint.visited = true;
        }

        ArrayList<Node> path = new ArrayList<>();
        if (jalan.penyimpananjalan[jalan.end.x][jalan.end.y].jarak != Integer.MAX_VALUE) {
            Node current = jalan.penyimpananjalan[jalan.end.x][jalan.end.y];
            while (current.parent != null) {
                path.add(current.parent);
                current = current.parent;
            }
        }
        return path;
    }

    private Queue<Node> helper(Alamatpath jalan, int x, int y, Node pinpoint, Queue<Node> queuejalan){
        Queue<Node> kiuw = queuejalan;
        Node check = jalan.penyimpananjalan[pinpoint.x + x][pinpoint.y + y];
        if (check.value != 0 && !check.visited && check.jarak > pinpoint.jarak + check.value){
            check.jarak = pinpoint.jarak + check.value;
            check.parent = pinpoint;
            kiuw.add(check);
        }
        return kiuw;
    }

    private Pickup powerUpTerdekat(Cell[][] peta){
        Pickup lokasiPowerup = new Pickup();
        int distance = 99;
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                if (peta[j][i].powerUp != null && peta[j][i].powerUp.type  == PowerUpType.HEALTH_PACK){
                    if (distance >= getDiagonalDistance(currentWorm.position.x, currentWorm.position.y, i, j)){
                        distance = getDiagonalDistance(currentWorm.position.x, currentWorm.position.y, i, j);
                        lokasiPowerup.x = i;
                        lokasiPowerup.y = j;
                        return lokasiPowerup;
                    }
                }
            }
        }
        return null;
    }

    private int getDiagonalDistance(int x,int y,int x1, int y1){
        return Math.max(Math.abs(x-x1),Math.abs(y-y1));
    }

    private int[][] mapsToGraph(Cell[][] apel){
        int graf[][] = new int [33][33];
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                // Don't include the current position
                if (apel[i][j].type == CellType.DIRT) graf[i][j] = 2;
                else if (apel[i][j].type == CellType.AIR) graf[i][j] = 1;
                else graf[i][j]= 0;
            }
        }
        return graf;
    }

    private List<Cell> getMaps() {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                // Don't include the current position
                if (isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }
        return cells;
    }

    private Worm getFirstWormInRange() {
        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }
        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }
        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }
        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }
}

package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {
    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    public static boolean[] isBananaBomb;
    public static boolean[] isSnowBall;

    static {
        isBananaBomb = new boolean[]{false, false, false};
    }

    static {
        isSnowBall = new boolean[]{false, false, false};
    }

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
        while(sirsak.get(i).parent != null) i++;
        if(sirsak.get(0).parent == null) return 0;
        return i-1;
    }

    public Command run() {
        // Worm enemyWorm = getFirstWormInRange();
        // if (enemyWorm != null) {
        //     Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
        //     return new ShootCommand(direction);
        // }
        // gameState.currentWormId = 0;
        // BATASAN HP CACING
        // return new SelectCommand(1,currentWorm.position.x+1,currentWorm.position.y);
        if (getCurrentWorm(gameState).health <= getCurrentWorm(gameState).initHP - 40 && powerUpTerdekat() != null){
            if(getFirstWormInRange(4) != null){
                return serang();
                // atau ga menghindar
            } else {
                return cariHelthPek();
            }
        } else {
            return serang();
            // return cariHelthPek();
        }
        // System.out.println(gameState.myPlayer.worms[1].banana.count);
        // System.out.println(gameState.myPlayer.worms[2].snow.count);
        // return serang();


//      return new DoNothingCommand();
    }
    
    private Command helpeh(int i) {
        if(currentWorm.prof == Profession.COMMANDO) return huntByID_c(i+1);
        else if (currentWorm.prof == Profession.AGENT) return huntByID_a(i+1);
        else if (currentWorm.prof == Profession.TECHNOLOGIST) return huntByID_t(i+1);
        return new DoNothingCommand();
    }
    
    private Command serang(){
        if (opponent.worms[1].health > 0) return helpeh(1);
        else if (opponent.worms[2].health > 0) return helpeh(2);
        else if (opponent.worms[0].health > 0) return helpeh(0);
        return new DoNothingCommand();
    }
    
    private Command cariHelthPek(){
        int[][] matrixmap = mapsToGraph();
        Pickup nearestpowup = powerUpTerdekat();
        // Ambil powerup
        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = cekPowerUpSekitar(surroundingBlocks);
        if (cellIdx != 99){
            Cell pickuplocation = surroundingBlocks.get(cellIdx);
            return new MoveCommand(pickuplocation.x, pickuplocation.y);
        } else {
            // mungkin kalo masih full health jangan diambil
            ArrayList<Node> apel = pathFinding(matrixmap,currentWorm.position.x,
                                    currentWorm.position.y,nearestpowup.x,nearestpowup.y);
            int lennn = cariParent(apel);
            if(matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 1) {
                if(unoccupied(apel.get(lennn).x, apel.get(lennn).y)) {
                    return new MoveCommand(apel.get(lennn).x, apel.get(lennn).y);
                } else if(getFirstWormInRange(4) != null) {
                    Direction shootdir = resolveDirection(currentWorm.position,getFirstWormInRange(4).position);
                    return new ShootCommand(shootdir);
                } else {
                    // move ke cell lain, misalnya gada lakuin sesuatu buat hindar donothing
                }
            } else if (matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 2) {
                return new DigCommand(apel.get(lennn).x, apel.get(lennn).y);
            }
        }
        return new DoNothingCommand();
    }
    
    
    private int cekPowerUpSekitar(List<Cell> surroundingBlocks){
        for (int i = 0; i < 9; i++){
            Cell pickuplocation = surroundingBlocks.get(i);
            if (surroundingBlocks.get(i).type != CellType.DEEP_SPACE){
                if (pickuplocation.powerUp.type == PowerUpType.HEALTH_PACK){
                    return i;
                }
            }    
        }
        return 99;
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

    private boolean unoccupied(int x, int y){
        int i,j;
        for(j=0; j<gameState.opponents.length; j++){
            for(i=0; i<gameState.opponents[j].worms.length; i++){
                if(x == gameState.opponents[j].worms[i].position.x
                    && y == gameState.opponents[j].worms[i].position.y){
                    return false;
                }
            }
        }
        for(i=0; i<gameState.myPlayer.worms.length; i++){
            if(x == gameState.myPlayer.worms[i].position.x
                && y == gameState.myPlayer.worms[i].position.y){
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

    private Pickup powerUpTerdekat(){
        Cell[][] peta = gameState.map;
        Pickup lokasiPowerup = new Pickup();
        int distance = 99;
        boolean ketemu = false;
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                if (peta[j][i].powerUp != null && peta[j][i].powerUp.type  == PowerUpType.HEALTH_PACK){
                    if (distance >= getDiagonalDistance(currentWorm.position.x, currentWorm.position.y, j, i)){
                        distance = getDiagonalDistance(currentWorm.position.x, currentWorm.position.y, j, i);
                        lokasiPowerup.x = j;
                        lokasiPowerup.y = i;
                        ketemu = true;
                    }
                }
            }
        }
        if (ketemu) return lokasiPowerup;
        else return null;
    }

    private int getDiagonalDistance(int x, int y, int x1, int y1){
        return Math.max(Math.abs(x-x1),Math.abs(y-y1));
    }

    private int[][] mapsToGraph(){
        Cell[][] apel = gameState.map;
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

    private Worm getFirstWormInRange(int range) {
        Set<String> cells = constructFireDirectionLines(range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition) && enemyWorm.health > 0) {
                return enemyWorm;
            }
        }
        return null;
    }

    private Position getWormLocationByID(int n){
        Position locTarget = new Position();
        for (Worm enemyWorm : opponent.worms){
            if(enemyWorm.id == n){
                locTarget.x = enemyWorm.position.x;
                locTarget.y = enemyWorm.position.y;
            }
        }
        return locTarget; 
    }

    private Command huntByID_c(int n) {
        int[][] matrixmap = mapsToGraph();
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false; //dijadiin var global??
        boolean isFriendlyFire = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n) {
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        Set<String> cells = constructFireDirectionLines(4)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;

        if (canAttack){
            Direction shootDir = resolveDirection(currentWorm.position, locTarget);
            Set<String> myline = constructShootLine(shootDir)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

            for (int i=0;i<3;i++){
                String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
                if(myline.contains(myPos)){
                    isFriendlyFire = true;
                    break;
                }
            }
        }
        
        if (!isFriendlyFire && canAttack){
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);            
        } else {
            ArrayList<Node> apel = pathFinding(matrixmap, currentWorm.position.x,
                    currentWorm.position.y, locTarget.x, locTarget.y);
            int lennn = cariParent(apel);
            if(matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 1
                && unoccupied(apel.get(lennn).x, apel.get(lennn).y)) {
                return new MoveCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else if (matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 2) {
                return new DigCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else {
                // move ke cell lain, misalnya gada lakuin sesuatu buat hindar donothing
            }
        }
        return new DoNothingCommand();
    }

    private Command huntByID_a(int n) {
        int[][] matrixmap = mapsToGraph();
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false; //dijadiin var global??
        boolean canBananaBomb = false;
        boolean isFriendlyFire = false;
        boolean isFriendlyFireB = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n && currentWorm.health >= 50){
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }
        Worm opp2 = getFirstWormInRange(5);
        if (currentWorm.health < 50 && opp2 != null && currentWorm.banana.count > 0){
            Position target = getWormLocationByID(n);
            return new BananaBombCommand(target.x,target.y);
        }

        Set<String> cells = constructFireDirectionLines(4)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        // Set<String> cellsB = constructFireDirectionLines(5)
        //         .stream()
        //         .flatMap(Collection::stream)
        //         .map(cell -> String.format("%d_%d", cell.x, cell.y))
        //         .collect(Collectors.toSet());

        Set<String> cellsBB = constructBananaBombArea(getWormLocationByID(n))
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        // System.out.println(getWormLocationByID(n).x);
        // System.out.println(getWormLocationByID(n).y);
        // List<String> mainList = new ArrayList<String>();
        // mainList.addAll(cellsB);
        // System.out.println(mainList.toString());

        // List<String> mainList2 = new ArrayList<String>();
        // mainList2.addAll(cells);
        // System.out.println(mainList2.toString());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;
        if (canSpecial(5) && currentWorm.banana.count > 0) canBananaBomb = true;

        if (canAttack){
            Direction shootDir = resolveDirection(currentWorm.position, locTarget);
            Set<String> myline = constructShootLine(shootDir)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

            for (int i=0;i<3;i++){
                String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
                if(myline.contains(myPos)){
                    isFriendlyFire = true;
                    break;
                }
            }
        }
        
        for (int i=0;i<3;i++){
            String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
            if(cellsBB.contains(myPos) && gameState.myPlayer.worms[i].health > 0){
                isFriendlyFireB = true;
                break;
            }
        }

        if (canBananaBomb){
            Position target = getWormLocationByID(n);
            isBananaBomb[n-1] = true;
            return new BananaBombCommand(target.x,target.y);

        } else if (canAttack && !isFriendlyFire) {
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);
        } else {
            ArrayList<Node> apel = pathFinding(matrixmap, currentWorm.position.x,
                                   currentWorm.position.y, locTarget.x, locTarget.y);
            int lennn = cariParent(apel);
            if(matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 1
                && unoccupied(apel.get(lennn).x, apel.get(lennn).y)) {
                return new MoveCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else if (matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 2) {
                return new DigCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else {
                // move ke cell lain, misalnya gada lakuin sesuatu buat hindar donothing
            }
        }
        return new DoNothingCommand();
    }

    private Command huntByID_t(int n) {
        int[][] matrixmap = mapsToGraph();
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false; //dijadiin var global??
        boolean isFriendlyFire = false;
        boolean isFriendlyFireS = false;
        boolean canSnowBall = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n){
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        Set<String> cells = constructFireDirectionLines(4)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        Set<String> cellsSB = constructSnowballArea(locTarget)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        Set<String> cellsS = constructFireDirectionLines(5)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;

        if (canAttack){
            Direction shootDir = resolveDirection(currentWorm.position, locTarget);
            Set<String> myline = constructShootLine(shootDir)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

            for (int i=0;i<3;i++){
                String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
                if(cells.contains(myPos)){
                    isFriendlyFire = true;
                    break;
                }
            }
        }

        for (int i=0;i<3;i++){
            String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
            if(cellsS.contains(myPos)){
                isFriendlyFireS = true;
                break;
            }
        }
        if (canSpecial(5) && currentWorm.snow.count > 0
        && opponent.worms[n-1].health > 0 && opponent.worms[n-1].sampeMeleleh == 0) canSnowBall = true;


        if (canSnowBall && !isFriendlyFireS){
            Position target = getWormLocationByID(n);
            // isSnowBall[n-1] = true;
            return new SnowballCommand(target.x,target.y);
        } else if (canAttack && !isFriendlyFire) {
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);
        } else {
            ArrayList<Node> apel = pathFinding(matrixmap, currentWorm.position.x,
                    currentWorm.position.y, locTarget.x, locTarget.y);
            int lennn = cariParent(apel);
            if(matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 1
                && unoccupied(apel.get(lennn).x, apel.get(lennn).y)) {
                return new MoveCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else if (matrixmap[apel.get(lennn).y][apel.get(lennn).x] == 2) {
                return new DigCommand(apel.get(lennn).x, apel.get(lennn).y);
            } else {
                // move ke cell lain, misalnya gada lakuin sesuatu buat hindar donothing
            }
        }
        return new DoNothingCommand();
    }

    private Boolean canSpecial (int n){
        for(int j=0; j<gameState.opponents.length; j++){
            for(int i=0; i<gameState.opponents[j].worms.length; i++){
                if( euclideanDistance(currentWorm.position.x,currentWorm.position.y,gameState.opponents[j].worms[i].position.x, gameState.opponents[j].worms[i].position.y) <=n){
                    return true;
                }
            }
        }
        return false;
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

    private List<Cell> constructBananaBombArea(Position target){
        List<Cell> area = new ArrayList<Cell>();
        for(int i=-2;i<3;i++)
            if(i != 0) area.add(gameState.map[target.y][target.x+i]); // HORIZONTAL
        for(int i=-2;i<3;i++)
            if(i != 0) area.add(gameState.map[target.y+i][target.x]); // VERTICAL

        // DIAGONAL
        area.add(gameState.map[target.y+1][target.x+1]);
        area.add(gameState.map[target.y-1][target.x+1]);
        area.add(gameState.map[target.y-1][target.x-1]);
        area.add(gameState.map[target.y-1][target.x+1]);
        
        area.add(gameState.map[target.y][target.x]); // CENTER
        return area;
    }

    private ArrayList<Cell> constructSnowballArea(Position target){
        ArrayList<Cell> area = new ArrayList<>();
        area.add(gameState.map[target.y][target.x]);
        area.add(gameState.map[target.y+1][target.x]);
        area.add(gameState.map[target.y-1][target.x]);
        area.add(gameState.map[target.y][target.x+1]);
        area.add(gameState.map[target.y][target.x-1]);
        area.add(gameState.map[target.y+1][target.x+1]);
        area.add(gameState.map[target.y+1][target.x-1]);
        area.add(gameState.map[target.y-1][target.x+1]);
        area.add(gameState.map[target.y-1][target.x-1]);
        return area;
    }
    
    private ArrayList<Cell> constructShootLine(Direction dir){
        ArrayList<Cell> line = new ArrayList<>();
        if (dir == Direction.NE) for(int i=1; i<4; i++) line.add(gameState.map[currentWorm.position.y+i][currentWorm.position.x-i]);
        if (dir == Direction.NW) for(int i=1; i<4; i++) line.add(gameState.map[currentWorm.position.y-i][currentWorm.position.x-i]);
        if (dir == Direction.N) for(int i=1; i<5; i++) line.add(gameState.map[currentWorm.position.y][currentWorm.position.x-i]);
        if (dir == Direction.S) for(int i=1; i<5; i++) line.add(gameState.map[currentWorm.position.y][currentWorm.position.x+i]);
        if (dir == Direction.SW) for(int i=1; i<4; i++) line.add(gameState.map[currentWorm.position.y-i][currentWorm.position.x+i]);
        if (dir == Direction.SE) for(int i=1; i<4; i++) line.add(gameState.map[currentWorm.position.y+i][currentWorm.position.x+i]);
        if (dir == Direction.W) for(int i=1; i<5; i++) line.add(gameState.map[currentWorm.position.y-i][currentWorm.position.x]);
        if (dir == Direction.E) for(int i=1; i<5; i++) line.add(gameState.map[currentWorm.position.y+i][currentWorm.position.x]);
        return line;
    }

    private ArrayList<Cell> getSurroundingCells(int x, int y) {
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

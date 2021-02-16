package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {
    // TODO: si agen butuh kuliah, first round perhaps masih ada choke, menghindar, select
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

    private int cariPanjang(ArrayList<Node> path){
        int i = 0;
        while(path.get(i).parent != null) i++;
        if(path.get(0).parent == null) return 0;
        return i-1;
    }

    public Command run() {
        // BATASAN HP CACING
        // if (getCurrentWorm(gameState).health <= getCurrentWorm(gameState).initHP - 40
        //     && powerUpTerdekat() != null){
        //     if(getFirstWormInRange(4) != null) return serang(); // atau ga menghindar
        //     else return cariHelthPek();
        // } else return serang();
        if (isSurrounded() && !isSurroundedF()){
            return keTemen();
        } else {
            return serang();
        }
    }
    
    private Command serang(){
        if (opponent.worms[1].health > 0) return helperSerang(1);
        else if (opponent.worms[2].health > 0) return helperSerang(2);
        else if (opponent.worms[0].health > 0) return helperSerang(0);
        return new DoNothingCommand();
    }
    
    private Command helperSerang(int i) {
        if(currentWorm.prof == Profession.COMMANDO) return huntByIDCommando(i+1);
        else if (currentWorm.prof == Profession.AGENT) return huntByIDAgent(i+1);
        else if (currentWorm.prof == Profession.TECHNOLOGIST) return huntByIDTechnologist(i+1);
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
            ArrayList<Node> apel = pathFinding(matrixmap,currentWorm.position.x,
                                    currentWorm.position.y,nearestpowup.x,nearestpowup.y);
            int panjang = cariPanjang(apel);
            if(getFirstWormInRange(4) != null) {
                Direction shootdir = resolveDirection(currentWorm.position,getFirstWormInRange(4).position);
                return new ShootCommand(shootdir);
            } else if (matrixmap[apel.get(panjang).y][apel.get(panjang).x] == 2) {
                return new DigCommand(apel.get(panjang).x, apel.get(panjang).y);
            } else {
                return new MoveCommand(apel.get(panjang).x, apel.get(panjang).y);
            }
        }
    }
    
    private int cekPowerUpSekitar(List<Cell> surroundingBlocks){
        for (int i = 0; i < 9; i++){
            Cell pickuplocation = surroundingBlocks.get(i);
            if (surroundingBlocks.get(i).type != CellType.DEEP_SPACE
                && pickuplocation.powerUp.type == PowerUpType.HEALTH_PACK)
                    return i;
        }
        return 99;
    }

    // HERE
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
                queuejalan = helperPath(jalan, 0, -1, pinpoint, queuejalan); // UP
                if (pinpoint.x + 1 < 33) queuejalan = helperPath(jalan, 1, -1, pinpoint, queuejalan); // RIGHT
                if (pinpoint.x - 1 >= 0) queuejalan = helperPath(jalan, -1, -1, pinpoint, queuejalan); // LEFT
            }
            if (pinpoint.y + 1 < 33){ // DOWN
                queuejalan = helperPath(jalan, 0, 1, pinpoint, queuejalan); // DOWN
                if (pinpoint.x + 1 < 33) queuejalan = helperPath(jalan, 1, 1, pinpoint, queuejalan); // RIGHT
                if (pinpoint.x - 1 >= 0) queuejalan = helperPath(jalan, -1, 1, pinpoint, queuejalan); // LEFT
            }
            if (pinpoint.x + 1 < 33) queuejalan = helperPath(jalan, 1, 0, pinpoint, queuejalan); // RIGHT
            if (pinpoint.x - 1 >= 0) queuejalan = helperPath(jalan, -1, 0, pinpoint, queuejalan); // LEFT
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

    private Queue<Node> helperPath(Alamatpath jalan, int x, int y, Node pinpoint, Queue<Node> queuejalan){
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
                if (peta[j][i].powerUp != null && peta[j][i].powerUp.type == PowerUpType.HEALTH_PACK){
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
                else if (apel[i][j].type == CellType.LAVA) graf[i][j] = 666;
                else graf[i][j]= 0;
            }
        }
        for(int k=0; k<3; k++){
            graf[getWormLocationByID(k+1).x][getWormLocationByID(k).y] = 69;
            graf[gameState.myPlayer.worms[k].position.x][gameState.myPlayer.worms[k].position.y] = 420;
        }
        return graf;
    }

    // HERE
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

    private Command huntByIDCommando(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false; //dijadiin var global??
        boolean isFriendlyFire = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n) {
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        Set<String> cells = makeCells(4);

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;

        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        
        if (!isFriendlyFire && canAttack){
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);            
        } else {
            return moveOrDig(mapsToGraph(), currentWorm.position.x, currentWorm.position.y,
                                locTarget.x, locTarget.y);
        }
    }

    private Command huntByIDAgent(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false;
        boolean isFriendlyFire = false;
        boolean isFriendlyFireB = false;
        boolean canBananaBomb = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n){
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }
        // ngabisin bom
        Worm opp2 = getFirstWormInRange(5);
        if (currentWorm.health < 50 && opp2 != null && currentWorm.banana.count > 0){
            Position target = getWormLocationByID(n);
            return new BananaBombCommand(target.x,target.y);
        }

        Set<String> cells = makeCells(4);
        // Set<String> cellsB = = makeCells(5);
        Set<String> cellsBB = constructBananaBombArea(getWormLocationByID(n))
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;
        if (canSpecialT(n,5) && currentWorm.banana.count > 0) canBananaBomb = true;

        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        isFriendlyFireB = isFriendlyFire1(cellsBB);
        
        if (canBananaBomb && !isFriendlyFireB){
            Position target = getWormLocationByID(n);
            return new BananaBombCommand(target.x,target.y);
        } else if (canAttack && !isFriendlyFire) {
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);
        } else {
            return moveOrDig(mapsToGraph(), currentWorm.position.x, currentWorm.position.y,
                                locTarget.x, locTarget.y);
        }
    }

    private Command huntByIDTechnologist(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false;
        boolean isFriendlyFire = false;
        boolean isFriendlyFireS = false;
        boolean canSnowBall = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && opp.id != n){
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }
        // ngabisin snobol
        Worm opp2 = getFirstWormInRange(5);
        if (currentWorm.health < 50 && opp2 != null && currentWorm.snow.count > 0){
            Position target = getWormLocationByID(n);
            return new SnowballCommand(target.x,target.y);
        }

        Set<String> cells = makeCells(4);
        // Set<String> cellsS = makeCells(5);
        Set<String> cellsSB = constructSnowballArea(locTarget)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;
        // if (canSpecial(5) && currentWorm.snow.count > 0) canSnowBall = true;
        // nunggu meleleh
        if (canSpecialT(n,5) && currentWorm.snow.count > 0
                && opponent.worms[n-1].health > 0 && opponent.worms[n-1].sampeMeleleh == 0)
                canSnowBall = true;

        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        isFriendlyFireS = isFriendlyFire1(cellsSB);

        if (canSnowBall && !isFriendlyFireS){
            Position target = getWormLocationByID(n);
            return new SnowballCommand(target.x,target.y);
        } else if (canAttack && !isFriendlyFire) {
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);
        } else {
            return moveOrDig(mapsToGraph(), currentWorm.position.x, currentWorm.position.y,
                                locTarget.x, locTarget.y);
        }
    }

    private Command moveOrDig(int[][] matrixmap, int x, int y, int xt, int yt){
        ArrayList<Node> apel = pathFinding(matrixmap, x, y, xt, yt);
        int panjang = cariPanjang(apel);
        if (matrixmap[apel.get(panjang).y][apel.get(panjang).x] == 2) {
            return new DigCommand(apel.get(panjang).x, apel.get(panjang).y);
        } else {
            return new MoveCommand(apel.get(panjang).x, apel.get(panjang).y);
        }
    }

    private Boolean canSpecial(int n){
        for(int i=0; i<gameState.opponents[0].worms.length; i++){
            if(euclideanDistance(currentWorm.position.x,currentWorm.position.y,
                gameState.opponents[0].worms[i].position.x,
                gameState.opponents[0].worms[i].position.y) <=n)
                return true;
        }
        return false;
    }

    private Boolean canSpecialT(int target, int n){
        for(int i=0; i<gameState.opponents[0].worms.length; i++){
            if(euclideanDistance(currentWorm.position.x,currentWorm.position.y,
                gameState.opponents[0].worms[i].position.x,
                gameState.opponents[0].worms[i].position.y) <= n && gameState.opponents[0].worms[i].id == target)
                return true;
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

    private Set<String> makeCells(int n){
        return constructFireDirectionLines(n)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());
    }

    private Set<String> makeLine(Direction shootDir){
        return constructShootLine(shootDir)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());
    }

    private ArrayList<Cell> constructBananaBombArea(Position target){
        ArrayList<Cell> area = new ArrayList<>();
        for(int i=-2;i<3;i++)
            if(i != 0 && target.x+i<33) area.add(gameState.map[target.y][target.x+i]); // HORIZONTAL
        for(int i=-2;i<3;i++)
            if(i != 0 && target.y+i<33) area.add(gameState.map[target.y+i][target.x]); // VERTICAL

        // DIAGONAL
        if (target.y+1<33 && target.x+1 < 33) area.add(gameState.map[target.y+1][target.x+1]);
        if (target.y-1<33 && target.x+1 < 33) area.add(gameState.map[target.y-1][target.x+1]);
        if (target.y-1<33 && target.x-1 < 33) area.add(gameState.map[target.y-1][target.x-1]);
        if (target.y-1<33 && target.x+1 < 33) area.add(gameState.map[target.y-1][target.x+1]);
        
        area.add(gameState.map[target.y][target.x]); // CENTER
        return area;
    }

    private ArrayList<Cell> constructSnowballArea(Position target){
        ArrayList<Cell> area = new ArrayList<>();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1;y++)
                area = helpeg(target, x, y, area);
        return area;
    }

    private ArrayList<Cell> helpeg(Position target, int x, int y, ArrayList<Cell> area){
        if (isValidCoordinate(target.x, target.y)) area.add(gameState.map[target.y+y][target.x+x]);
        return area;
    }
    
    private ArrayList<Cell> constructShootLine(Direction dir){
        ArrayList<Cell> line = new ArrayList<>();
        for(Direction direction : Direction.values()) {
            line = direction.x != 0 && direction.y != 0 ? helpep(4, direction.x, direction.y, line) : helpep(5, direction.x, direction.y, line);
        }
        return line;
    }
    
    private ArrayList<Cell> helpep(int range, int x, int y, ArrayList<Cell> line){
        for(int i = 1; i < range; i++)
            if (isValidCoordinate(currentWorm.position.x + x, currentWorm.position.y + y))
                line.add(gameState.map[currentWorm.position.y + y][currentWorm.position.x + x]);
        return line;
    }

    private boolean isFriendlyFire0(boolean canAttack, Position locTarget){
        if (canAttack){
            Direction shootDir = resolveDirection(currentWorm.position, locTarget);
            Set<String> myline = makeLine(shootDir);
    
            for (int i=0;i<3;i++){
                String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
                if(myline.contains(myPos)) return true;
            }
        }
        return false;
    }

    private boolean isFriendlyFire1(Set<String> cells){
        for (int i=0;i<3;i++){
            String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
            if(cells.contains(myPos) && gameState.myPlayer.worms[i].health > 0)
                return true;
        }
        return false;
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

    private boolean isSurrounded(){
        int count = 0;
        for (Worm enemyWorm : opponent.worms)
            if(euclideanDistance(currentWorm.position.x,currentWorm.position.y,
                            enemyWorm.position.x,enemyWorm.position.y) <= 5)
                count += 1;
        return count>1;
    }

    private boolean isSurroundedF(){
        int count = 0;
        for (Worm friend : gameState.myPlayer.worms)
            if(euclideanDistance(currentWorm.position.x,currentWorm.position.y,
                            friend.position.x,friend.position.y) <= 5)
                count += 1;
        return count>1;
    }

    private Command kaburEuy(){
        // Worm opp = getFirstWormInRange(4);
        // int[][] peta = mapsToGraph();
        // Direction oppDir;
        // int cacingx = currentWorm.position.x;
        // int cacingy = currentWorm.position.y;
        // if (opp != null && opp.health > 0){
        //     oppDir = resolveDirection(opp.position, currentWorm.position);
        //     if (oppDir == Direction.E || oppDir == Direction.W){
        //         if (map[cacingx][cacingy-1] != 666 && map[cacingx][cacingy-1] != 0 ){
        //             moveOrDig(mapsToGraph(), cacingx, cacingy, cacingx, cacingy-1);
        //         else
        //         }
        //     }
        // List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        // System.out.println(surroundingBlocks.size());

        // for(int i = 0;i <= surroundingBlocks.size(); i++){
        //     Cell block = surroundingBlocks.get(i);
        //     if (block.type != CellType.LAVA && block.type != CellType.DEEP_SPACE
        //     && getFirstWormInRange(4) == null && unoccupied(block.x,block.y)){
        //         break;
        //     }
        // }


        // if (block.type == CellType.AIR) {
        //     return new MoveCommand(block.x, block.y);
        // } else if (block.type == CellType.DIRT) {
        //     return new DigCommand(block.x, block.y);
        // } else{
            return new DoNothingCommand();
        // }
        
        // Worm locTarget = getFirstWormInRange(5);
        // int kaburX = 0;
        // int kaburY = 0;
        // for(int i=15;i>=1;i--){
        //     if (locTarget != null && locTarget.position.x + i < 33 && locTarget.position.y + i < 33){
        //         kaburX = locTarget.position.x + i;
        //         kaburY = locTarget.position.y + i;
        //         break;
        //     }
        // }
        // return moveOrDig(mapsToGraph(), currentWorm.position.x,currentWorm.position.y, kaburX, kaburY);

    }

    private Command keTemen(){
        double min = 0;
        int id_min = 0;
        for (Worm friend : gameState.myPlayer.worms){
            double distance = euclideanDistance(currentWorm.position.x,currentWorm.position.y,friend.position.x,friend.position.y);
            if (distance <= min){
                min = distance;
                id_min = friend.id;
            }
        }
        return moveOrDig(mapsToGraph(),currentWorm.position.x,currentWorm.position.y,gameState.myPlayer.worms[id_min-1].position.x,gameState.myPlayer.worms[id_min-1].position.y);
    }

    private boolean unoccupied(int x, int y){
        for(int i=0; i<3; i++){
            if((x == gameState.opponents[0].worms[i].position.x
                && y == gameState.opponents[0].worms[i].position.y)
                || (x == gameState.myPlayer.worms[i].position.x
                && y == gameState.myPlayer.worms[i].position.y)){
                return false;
            }
        }
        return true;
    }
}
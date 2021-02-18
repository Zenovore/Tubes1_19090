package za.co.entelect.challenge;
import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO:
 * lengkapin komen stima greedy
 * ALEX
 */

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

    /**
     * Metode untuk mengeksekusi bot
     * @return command
     */
    public Command run() {
        if (currentWorm.health < 30) {
            return kaburEuy();
        }
        return serang();
    }

    /**
     * Metode untuk menyerang musuh berdasarkan prioritas:
     * Agent, Technologist, Commando
     * @return command
     */
    private Command serang() {
        if (opponent.worms[1].health > 0) return helperSerang(1);
        else if (opponent.worms[2].health > 0) return helperSerang(2);
        else if (opponent.worms[0].health > 0) return helperSerang(0);
        return new DoNothingCommand();
    }

    /**
     * Metode untuk menyerang musuh sesuai dengan profession currentWorm
     * @return command
     */
    private Command helperSerang(int i) {
        if (currentWorm.prof == Profession.COMMANDO) return huntByIDCommando(i+1);
        else if (currentWorm.prof == Profession.AGENT) return huntByIDAgent(i+1);
        else if (currentWorm.prof == Profession.TECHNOLOGIST) return huntByIDTechnologist(i+1);
        return new DoNothingCommand();
    }

    /**
     * Metode untuk mencari panjang path
     * @param path bertipe Alamatpath
     * @return panjang path atau banyak node
     */
    private int cariPanjang(ArrayList<Node> path) {
        int i = 0;
        if (path.get(i).parent == null) return 0;
        while(path.get(i).parent != null) i++;
        return i-1;
    }

    /**
     * Metode untuk mencari jalan terpendek (Djikstra)
     * @param peta peta game dalam bentuk matriks integer
     * @param x titik x awal
     * @param yend titik y awal
     * @param x titik x tujuan
     * @param yend titik y tujuan
     * @return ArrayList node dari asal ke tujuan
     */
    private ArrayList<Node> pathFinding(int[][] peta, int x, int y, int xend, int yend) {
        Alamatpath jalan = new Alamatpath(x, y, xend, yend, 33);
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

        while (queuejalan.size() > 0) {
            Node pinpoint = queuejalan.remove();
            // ALGORITMA UNTUK MENGECEK PERJALANAN KE 8 ARAH
            if (pinpoint.y - 1 >= 0) {
                queuejalan = helperPath(jalan, 0, -1, pinpoint, queuejalan); // UP
                if (pinpoint.x + 1 < 33) queuejalan = helperPath(jalan, 1, -1, pinpoint, queuejalan); // RIGHT
                if (pinpoint.x - 1 >= 0) queuejalan = helperPath(jalan, -1, -1, pinpoint, queuejalan); // LEFT
            }
            if (pinpoint.y + 1 < 33) { // DOWN
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

    /**
     * Metode untuk membantu mempersingkat algoritma metode pathFinding
     * Metode mengecek dan menambahkan apabila titik memenuhi syarat
     * @param jalan Alamatpath
     * @param x nilai arah x
     * @param y nilai arah y
     * @param pinpoint Node terakhir, sebagai parent
     * @param queuejalan ALEX
     * @return ALEX
     */
    private Queue<Node> helperPath(Alamatpath jalan, int x, int y, Node pinpoint, Queue<Node> queuejalan) {
        Queue<Node> kiuw = queuejalan;
        Node check = jalan.penyimpananjalan[pinpoint.x + x][pinpoint.y + y];
        if (check.value != 0 && !check.visited && check.jarak > pinpoint.jarak + check.value) {
            check.jarak = pinpoint.jarak + check.value;
            check.parent = pinpoint;
            kiuw.add(check);
        }
        return kiuw;
    }

    /**
     * Metode untuk mengubah peta dari gamestate ke matriks integer
     * @return map dalam bentuk matriks integer
     */
    private int[][] mapsToGraph() {
        Cell[][] apel = gameState.map;
        int graf[][] = new int[33][33];
        for (int i = 0; i < 33; i++) {
            for (int j = 0; j < 33; j++) {
                // Don't include the current position
                if (apel[i][j].type == CellType.DIRT) graf[i][j] = 2;
                else if (apel[i][j].type == CellType.AIR) graf[i][j] = 1;
                else if (apel[i][j].type == CellType.LAVA) graf[i][j] = 666;
                else graf[i][j]= 0;
            }
        }
        for (int k = 0; k < 3; k++) {
            graf[getWormLocationByID(k+1).x][getWormLocationByID(k+1).y] = 69;
            graf[gameState.myPlayer.worms[k].position.x][gameState.myPlayer.worms[k].position.y] = 420;
        }
        return graf;
    }

    /**
     * Metode untuk mencari cacing di sekitar cacing currentWorm
     * @param range range tembak cacing (8 arah)
     * @return worm dalam range tembak cacing
     */
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

    /**
     * Metode untuk mencari cacing pemain di sekitar cacing musuh
     * @param range range tembak cacing musuh
     * @param block cell lokasi cacing pemain atau cacing target
     * @param id id cacing musuh - 1
     * @return true: ada cacing pemain yang dapat ditembak
     */
    private boolean getFirstWormInRangeReversed(int range, Cell block, int id) {
        Set<String> cells = constructFireDirectionLinesReversed(range, id)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        String myPosition = String.format("%d_%d", block.x, block.y);
        if (cells.contains(myPosition)) return true;
        return false;
    }

    /**
     * Metode untuk mencari lokasi cacing musuh menggunakan id
     * @param n id cacing musuh
     * @return posisi cacing musuh dengan id n
     */
    private Position getWormLocationByID(int n) {
        Position locTarget = new Position();
        for (Worm enemyWorm : opponent.worms) {
            if (enemyWorm.id == n) {
                locTarget.x = enemyWorm.position.x;
                locTarget.y = enemyWorm.position.y;
            }
        }
        return locTarget; 
    }

    /**
     * Metode untuk menyerang musuh jika cacing berprofesi Commando
     * @param n id cacing musuh yang ingin diserang
     * @return command
     */
    private Command huntByIDCommando(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false;
        boolean isFriendlyFire = false;

        Worm opp = getFirstWormInRange(4);
        if (opp != null && !isFriendlyFire0(true, opp.position)) {
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        Set<String> cells = makeCells(4);

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;

        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        
        if (!isFriendlyFire && canAttack) {
            Direction shootdir = resolveDirection(currentWorm.position, locTarget);
            return new ShootCommand(shootdir);            
        } else {
            return moveOrDig(mapsToGraph(), currentWorm.position.x, currentWorm.position.y,
                                locTarget.x, locTarget.y);
        }
    }

    /**
     * Metode untuk menyerang musuh jika cacing berprofesi Agent
     * @param n id cacing musuh yang ingin diserang
     * @return command
     */
    private Command huntByIDAgent(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false;
        boolean isFriendlyFire = false;
        boolean isFriendlyFireB = false;
        boolean canBananaBomb = false;

        Set<String> cells = makeCells(4);
        Set<String> cellsBB = constructBananaBombArea(locTarget)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;
        if (canSpecialT(n,5) && currentWorm.banana.count > 0) canBananaBomb = true;

        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        isFriendlyFireB = isFriendlyFire1(cellsBB);
        
        // ngabisin bom
        for (int i = 0; i < 3; i++) {
            if (opponent.worms[i].health > 0 && canSpecialT(i+1, 5)
                && isFriendlyFireB
                && currentWorm.banana.count > 0) {
                Position target = opponent.worms[i].position;
                return new BananaBombCommand(target.x,target.y);
            }
        }

        Worm opp = getFirstWormInRange(4);
        if (opp != null && isFriendlyFire0(true, opp.position)) {
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        if (canBananaBomb && !isFriendlyFireB) {
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

    /**
     * Metode untuk menyerang musuh jika cacing berprofesi Technologist
     * @param n id cacing musuh yang ingin diserang
     * @return command
     */
    private Command huntByIDTechnologist(int n) {
        Position locTarget = getWormLocationByID(n);
        String oppPosition = String.format("%d_%d", locTarget.x, locTarget.y);
        boolean canAttack = false;
        boolean isFriendlyFire = false;
        boolean isFriendlyFireS = false;
        boolean canSnowBall = false;

        Set<String> cells = makeCells(4);
        Set<String> cellsSB = constructSnowballArea(locTarget)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        if (cells.contains(oppPosition) && opponent.worms[n-1].health > 0) canAttack = true;
        if (canSpecialT(n,5) && currentWorm.snow.count > 0
                && opponent.worms[n-1].health > 0 && opponent.worms[n-1].sampeMeleleh == 0)
                canSnowBall = true;
        
        isFriendlyFire = isFriendlyFire0(canAttack, locTarget);
        isFriendlyFireS = isFriendlyFire1(cellsSB);
                
        // ngabisin snobol
        for (int i = 0; i < 3; i++) {
            if (opponent.worms[i].health > 0 && canSpecialT(i+1, 5)
                && currentWorm.snow.count > 0 && isFriendlyFireS
                && opponent.worms[i].sampeMeleleh == 0) {
                Position target = opponent.worms[i].position;
                return new SnowballCommand(target.x,target.y);
            }
        }

        Worm opp = getFirstWormInRange(4);
        if (opp != null && !isFriendlyFire0(true, opp.position)) {
            Direction shootdir = resolveDirection(currentWorm.position, opp.position);
            return new ShootCommand(shootdir);
        }

        if (canSnowBall && !isFriendlyFireS) {
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

    /**
     * Metode untuk melakukan command move atau dig
     * @param matrixmap peta dalam bentuk matriks integer
     * @param x posisi x awal
     * @param y posisi y awal
     * @param xt posisi x akhir
     * @param yt posisi y akhir
     * @return command move atau dig
     */
    private Command moveOrDig(int[][] matrixmap, int x, int y, int xt, int yt) {
        ArrayList<Node> apel = pathFinding(matrixmap, x, y, xt, yt);
        int panjang = cariPanjang(apel);
        if (matrixmap[apel.get(panjang).y][apel.get(panjang).x] == 2) {
            return new DigCommand(apel.get(panjang).x, apel.get(panjang).y);
        } else {
            return new MoveCommand(apel.get(panjang).x, apel.get(panjang).y);
        }
    }

    /**
     * Metode untuk mengecek apakah cacing dapat melakukan
     * snowball atau banana bomb sesuai dengan jarak euclidean
     * @param target id cacing musuh
     * @param n jarak euclidean maksimum
     * @return true: jarak cacing target ke cacing pemain <= n
     */
    private Boolean canSpecialT(int target, int n) {
        if (euclideanDistance(currentWorm.position.x,currentWorm.position.y,
            gameState.opponents[0].worms[target-1].position.x,
            gameState.opponents[0].worms[target-1].position.y) <= n)
            return true;
        return false;
    }

    /**
     * Metode untuk membentuk semua garis tembak yang memungkinkan
     * Metode digunakan jika ingin mencari semua garis tembak dari cacing pemain
     * @param range jarak cell maksimum
     * @return semua arah tembak yang memungkinkan
     */
    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {
                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);
                if (!isValidCoordinate(coordinateX, coordinateY)) break;
                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) break;
                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) break;
                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }
        return directionLines;
    }

    /**
     * Metode untuk membentuk semua garis tembak yang memungkinkan
     * Metode digunakan jika ingin mencari semua garis tembak dari cacing musuh
     * @param range jarak cell maksimum
     * @return semua arah tembak yang memungkinkan
     */
    private List<List<Cell>> constructFireDirectionLinesReversed(int range, int id) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {
                int coordinateX = opponent.worms[id].position.x + (directionMultiplier * direction.x);
                int coordinateY = opponent.worms[id].position.y + (directionMultiplier * direction.y);
                if (!isValidCoordinate(coordinateX, coordinateY)) break;
                if (euclideanDistance(opponent.worms[id].position.x, opponent.worms[id].position.y, coordinateX, coordinateY) > range) break;
                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) break;
                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }
        return directionLines;
    }

    /**
     * Metode untuk membuat set dari semua arah tembak cacing yang memungkinkan
     * @param n range tembak cacing
     * @return set string dari semua cell dalam bentuk string x_y
     */
    private Set<String> makeCells(int n) {
        return constructFireDirectionLines(n)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());
    }

    /**
     * Metode untuk membuat set dari satu arah tembak cacing yang memungkinkan
     * Metode digunakan untuk membuat arah tembak dari cacing pemain
     * @param shootDir arah tembak cacing pemain
     * @return set string dari semua cell dalam bentuk string x_y
     */
    private Set<String> makeLine(Direction shootDir) {
        return constructShootLine(shootDir)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());
    }

    /**
     * Metode untuk membuat set dari satu arah tembak cacing yang memungkinkan
     * Metode digunakan untuk membuat arah tembak dari cacing musuh
     * @param shootDir arah tembak cacing musuh
     * @param enemyx posisi x musuh
     * @param enemyy posisi y musuh
     * @return set string dari semua cell dalam bentuk string x_y
     */
    private Set<String> makeLineEnemy(Direction shootDir, int enemyx, int enemyy) {
        return constructShootLineEnemy(shootDir, enemyx, enemyy)
                .stream()
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());
    }

    /**
     * Metode untuk membuat kumpulan cell yang terkena damage
     * sebuah banana bomb dalam sebuah ronde
     * @param target posisi target cacing yang ingin dibom
     * @return semua cell yang dapat damage banana bomb
     */
    private ArrayList<Cell> constructBananaBombArea(Position target) {
        ArrayList<Cell> area = new ArrayList<>();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                area = helpeg(target, x, y, area);
        area = helpeg(target, -2, 0, area);
        area = helpeg(target, 0, 2, area);
        area = helpeg(target, 2, 0, area);
        area = helpeg(target, 0, -2, area);
        return area;
    }

    /**
     * Metode untuk membuat kumpulan cell yang terkena efek
     * sebuah snowball dalam sebuah ronde
     * @param target posisi target cacing yang ingin dibom
     * @return semua cell yang dapat efek  snowball
     */
    private ArrayList<Cell> constructSnowballArea(Position target) {
        ArrayList<Cell> area = new ArrayList<>();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1;y++)
                area = helpeg(target, x, y, area);
        return area;
    }

    /**
     * Metode untuk menambahkan cell ke arraylist
     * @param area list yang ingin diappend
     * @return list
     */
    private ArrayList<Cell> helpeg(Position target, int x, int y, ArrayList<Cell> area) {
        if (isValidCoordinate(target.x+x, target.y+y)) area.add(gameState.map[target.y+y][target.x+x]);
        return area;
    }

    /**
     * Metode untuk membuat list garis tembak sesuai dengan
     * arah tembakan cacing pemain
     * @param dir arah tembakan cacing pemain
     * @return list cell yang ada dalam arah tembakan cacing pemain
     */
    private ArrayList<Cell> constructShootLine(Direction dir) {
        ArrayList<Cell> line = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction == dir) {
                line = direction.x != 0 && direction.y != 0 ?
                        helpep(4, direction.x, direction.y, line):
                        helpep(5, direction.x, direction.y, line);
            }
        }
        return line;
    }

    /**
     * Metode untuk memambahkan cell menjadi line
     * @param range range tembak maksimum
     * @param x satuan unit arah horizontal
     * @param y satuan unit arah vertikal
     * @param line arraylist
     * @return garis tembak
     */
    private ArrayList<Cell> helpep(int range, int x, int y, ArrayList<Cell> line) {
        for (int i = 1; i < range; i++)
            if (isValidCoordinate(currentWorm.position.x + i*x, currentWorm.position.y + i*y))
                line.add(gameState.map[currentWorm.position.y + i*y][currentWorm.position.x + i*x]);
        return line;
    }

    /**
     * Metode untuk membuat list garis tembak sesuai dengan
     * arah tembakan cacing musuh
     * @param dir arah tembakan cacing pemain
     * @param enemyx posisi x musuh
     * @param enemyy posisi y musuh
     * @return list cell yang ada dalam arah tembakan cacing musuh
     */
    private ArrayList<Cell> constructShootLineEnemy(Direction dir, int enemyx, int enemyy) {
        ArrayList<Cell> line = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (direction == dir) {
                line = direction.x != 0 && direction.y != 0 ?
                        helpepEnemy(4, direction.x, direction.y, line, enemyx, enemyy):
                        helpepEnemy(5, direction.x, direction.y, line, enemyx, enemyy);
            }
        }
        return line;
    }

    /**
     * Metode untuk memambahkan cell menjadi line
     * @param range range tembak maksimum
     * @param x satuan unit arah horizontal
     * @param y satuan unit arah vertikal
     * @param line arraylist
     * @param enemyx posisi x musuh
     * @param enemyy posisi y musuh
     * @return garis tembak
     */
    private ArrayList<Cell> helpepEnemy(int range, int x, int y, ArrayList<Cell> line, int enemyx, int enemyy) {
        for (int i = 1; i < range; i++)
            if (isValidCoordinate(enemyx + i*x, enemyy + i*y))
                line.add(gameState.map[enemyy + i*y][enemyx + i*x]);
        return line;
    }

    /**
     * Metode untuk mengecek apakah tembakan cacing pemain
     * dapat melukai cacing teman lainnya
     * @param canAttack kondisi terdapat cacing musuh di 8 arah tembak cacing
     * @param locTarget posisi tujuan tembak
     * @return true: tidak ada cacing teman dalam garis tembak cacing pemain
     */
    private boolean isFriendlyFire0(boolean canAttack, Position locTarget) {
        if (canAttack) {
            Direction shootDir = resolveDirection(currentWorm.position, locTarget);
            Set<String> myline = makeLine(shootDir);

            for (int i = 0; i < 3; i++) {
                String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
                if (myline.contains(myPos)) return true;
            }
        }
        return false;
    }

    /**
     * Metode untuk mengecek apakah banana bomb atau snowball dapat
     * melukai atau memberikan dampak pada cacing teman
     * @param cells area yang dapat ditarget oleh banana bomb atau snowball
     * @return true: tidak ada cacing teman dalam area cells
     */
    private boolean isFriendlyFire1(Set<String> cells) {
        for (int i = 0; i < 3; i++) {
            String myPos = String.format("%d_%d", gameState.myPlayer.worms[i].position.x, gameState.myPlayer.worms[i].position.y);
            if (cells.contains(myPos) && gameState.myPlayer.worms[i].health > 0)
                return true;
        }
        return false;
    }

    /**
     * Metode untuk mencari semua cell di sekitar posisi x dan y
     * @param x posisi x
     * @param y posisi y
     * @return semua cell di sekitar cell posisi x dan y
     */
    private ArrayList<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++)
            for (int j = y - 1; j <= y + 1; j++)
                if ((i != x || j != y) && isValidCoordinate(i, j))
                    cells.add(gameState.map[j][i]);
        return cells;
    }

    /**
     * Metode untuk mencari jarak euclidean
     * @param aX x a
     * @param aY y a
     * @param bX x b
     * @param bY y b
     * @return jarak euclidean
     */
    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    /**
     * Metode untuk memvalidasi posisi x, y ada di peta atau tidak
     * @param x posisi x
     * @param y posisi y
     * @return true: x, y ada di peta
     */
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    /**
     * Metode untuk mencari arah mata angin dari posisi a ke posisi b
     * @param a posisi awal
     * @param b posisi akhir
     * @return arah mata angin
     */
    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();
        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) builder.append('N');
        else if (verticalComponent > 0) builder.append('S');

        if (horizontalComponent < 0) builder.append('W');
        else if (horizontalComponent > 0) builder.append('E');
        try {
            return Direction.valueOf(builder.toString());
        } catch (IllegalArgumentException i) {
            return null;
        }        
    }

    /**
     * Metode untuk mengecek apakah posisi block dapat ditembak oleh musuh atau tidak
     * Metode digunakan untuk mengecek apakah cell block tujuan dapat ditembak
     * atau tidak oleh semua musuh yang hidup dan apakah cell tujuan dalam
     * garis posisi currentWorm dengan musuh
     * @param block
     * @return true: block dapat ditembak oleh cacing musuh yang berjalan sekarang dan akan miss
     */
    private boolean isInEnemyLineOfFire(Cell block) {
        Position locTarget = new Position();
        locTarget.x = block.x;
        locTarget.y = block.y;
        int i = gameState.opponents[0].currentWormId - 1;
        boolean a = false;
        boolean b = false;
        boolean c = false;
        for (int k = 0; k < 3; k++) {
            if (opponent.worms[k].health > 0 && getFirstWormInRangeReversed(5,block,k)) {
                Direction shootDir = resolveDirection(opponent.worms[k].position, locTarget);
                if (shootDir != null) {
                    Set<String> myline = makeLineEnemy(shootDir, opponent.worms[k].position.x, opponent.worms[k].position.y);
                    String myPos = String.format("%d_%d", locTarget.x, locTarget.y);
                    String wormPos = String.format("%d_%d",currentWorm.position.x,currentWorm.position.y);
                    if (k == i && myline.contains(myPos) && !myline.contains(wormPos)) {
                        // ini kalo musuhnya yg skrg bakal jalan, trus dia bisa nembak kita
                        // dan arah kita ke dia dan arah kita ke tujuan ga sama, kita buat true
                        if (k == 0) a = true;
                        if (k == 1) b = true;
                        if (k == 2) c = true;
                    }
                    if (k != i && myline.contains(myPos)) {
                        // ini kalo musuhnya bukan yg skrg bakal jalan
                        if (k == 0) a = true;
                        if (k == 1) b = true;
                        if (k == 2) c = true;
                    }
                }
            } else if (opponent.worms[k].health <= 0) {
                if (k == 0) a = true;
                if (k == 1) b = true;
                if (k == 2) c = true;
            }
        }
        return a && b && c;
    }

    /**
     * Metode untuk menghindar dari (serangan) musuh
     * Metode ini mencari cell di sekitar posisi currentWorm
     * Jika cell sekitar cacing memenuhi syarat maka cacing akan move atau dig ke cell tersebut
     * Jika tidak maka cacing akan menyerang musuh
     * Syarat cell tujuan: cell tidak diduduki oleh cacing lain, cell bukan lava atau deep space,
     * berada di dalam range lower dan upper, jauh dari lava, berada dalam jangkauan tembak musuh
     * selanjutnya.
     * Jika syarat terakhir (berada dalam jangkauan tembak musuh selanjutnya) tidak terpenuhi
     * maka cacing akan menjauhi cacing musuh.
     * @param lower
     * @param upper
     * @return command
     */
    private Command kaburEuyHelper(int lower, int upper) {
        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        boolean ketemu = false;
        Cell block = surroundingBlocks.get(0);
        int xtanah = 0;
        int ytanah = 0;

        for (int i = 0; i < surroundingBlocks.size(); i++) {
            block = surroundingBlocks.get(i);
            if (block.type != CellType.LAVA && block.type != CellType.DEEP_SPACE
                && isInEnemyLineOfFire(block) && unoccupied(block.x,block.y)
                && block.x <= upper && block.x >= lower && block.y >= lower && block.y <= upper
                && isAwayFromLava(block)
                && !(block.x == currentWorm.position.x && block.y == currentWorm.position.y)) {
                if (block.type == CellType.DIRT) {
                    ketemu = true;
                    xtanah = block.x;
                    ytanah = block.y;
                }else{
                    ketemu = true;
                    break;
                }
            }
        }
        if (ketemu && currentWorm.health <= 25) {
            if (block.type == CellType.DIRT && mapsToGraph()[ytanah][xtanah] == 2)
                return new DigCommand(xtanah, ytanah);
            else
                return new MoveCommand(block.x, block.y);
        }
        for (int i = 0; i < surroundingBlocks.size(); i++) {
            block = surroundingBlocks.get(i);
            if (block.type != CellType.LAVA && block.type != CellType.DEEP_SPACE
                && unoccupied(block.x,block.y) && isAwayFromLava(block) && isAwayFromEnemy(block)
                && block.x <= upper && block.x >= lower && block.y >= lower && block.y <= upper
                && !(block.x == currentWorm.position.x && block.y == currentWorm.position.y)) {
                if (block.type == CellType.DIRT) {
                    ketemu = true;
                    xtanah = block.x;
                    ytanah = block.y;
                }else{
                    ketemu = true;
                    break;
                }
            }
        }
        if (ketemu && currentWorm.health <= 25) {
            if (block.type == CellType.DIRT && mapsToGraph()[ytanah][xtanah] == 2)
                return new DigCommand(xtanah, ytanah);
            else
                return new MoveCommand(block.x, block.y);
        }
        return serang();
    }

    /**
     * Metode untuk menghindar dari serangan musuh
     * @return command
     */
    private Command kaburEuy() {
        // insert lava function, but kuli
        if (gameState.currentRound < 100) return kaburEuyHelper(4, 29);
        else if (gameState.currentRound < 120) return kaburEuyHelper(8, 20);
        else if (gameState.currentRound < 150) return kaburEuyHelper(9, 21);
        else if (gameState.currentRound < 180) return kaburEuyHelper(9, 21);
        else if (gameState.currentRound < 200) return kaburEuyHelper(10, 22);
        else if (gameState.currentRound < 230) return kaburEuyHelper(10, 22);
        else if (gameState.currentRound < 260) return kaburEuyHelper(11, 21);
        else if (gameState.currentRound < 290) return kaburEuyHelper(12, 20);
        else return kaburEuyHelper(13, 19);
    }

    /**
     * Metode untuk mengecek apakah cell block tujuan sama dengan posisi cacing musuh
     * @param block
     * @return true: block bukan merupakan posisi cacing musuh
     */
    private boolean isAwayFromEnemy(Cell block) {
        int idMusuh = gameState.opponents[0].currentWormId -1;
        if (block.x  == gameState.opponents[0].worms[idMusuh].position.x
            && block.y  == gameState.opponents[0].worms[idMusuh].position.y)
            return false;
        return true;
    }    

    /**
     * Metode untuk mencari apakah cell sekitar block ada lava
     * @param block
     * @return true: cell sekitar block tidak ada lava
     */
    private boolean isAwayFromLava(Cell block) {
        if (gameState.currentRound < 280 && gameState.currentRound > 100) {
            List<Cell> surroundingBlocks = getSurroundingCells(block.x, block.y);
            for (int i = 0; i < surroundingBlocks.size(); i++)
                if (surroundingBlocks.get(i).type == CellType.LAVA) 
                    return false;
        }
        return true;
    }

    /**
     * Metode untuk mengecek apakah posisi x, y pada peta ditempati oleh sebuah cacing
     * @param x posisi x
     * @param y posisi y
     * @return true: posisi x, y tidak ditempati oleh cacing lain
     */
    private boolean unoccupied(int x, int y) {
        for (int i = 0; i < 3; i++) {
            if ((x == gameState.opponents[0].worms[i].position.x
                && y == gameState.opponents[0].worms[i].position.y)
                || (x == gameState.myPlayer.worms[i].position.x
                && y == gameState.myPlayer.worms[i].position.y)) {
                return false;
            }
        }
        return true;
    }
}

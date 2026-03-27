package com.poopblock.russian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    // 游戏常量
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 20;
    private static final int GAME_SPEED = 1000; // 1秒下落一次
    private static final int DEFAULT_BLOCK_SIZE = 30; // 固定方块大小
    
    // 游戏状态
    private boolean isGameRunning = false;
    private int score = 0;
    private long gameStartTime = 0;
    private int gameDuration = 0; // 游戏时长（秒）
    private boolean isControllerEnabled = false;
    private boolean isControllerConnected = false;
    private boolean isGameSoundEnabled = true;
    
    // 方块类型对应的资源ID
    private final int[] BLOCK_ICONS = {
            R.drawable.egg,      // I型 - 鸡蛋
            R.drawable.cabbage,  // O型 - 白菜
            R.drawable.cigarette,// T型 - 烟头
            R.drawable.poop_block,// L型 - 粑粑
            R.drawable.poop_block,// J型 - 粑粑
            R.drawable.stone,    // S型 - 石头
            R.drawable.stone     // Z型 - 石头
    };
    
    // 方块类型对应的音效ID
    private final int[] BLOCK_SOUNDS = {
            R.raw.jidan,     // I型 - 鸡蛋
            R.raw.baicai,   // O型 - 白菜
            R.raw.yantou,   // T型 - 烟头
            R.raw.baba,     // L型 - 粑粑
            R.raw.baba,     // J型 - 粑粑
            R.raw.shitou,   // S型 - 石头
            R.raw.shitou    // Z型 - 石头
    };
    
    // 结束音效ID
    private final int GAME_OVER_SOUND = R.raw.jieshu;
    
    // 音效播放
    private android.media.SoundPool soundPool;
    private int clickSoundId;
    private int invalidOperationSoundId;
    private int currentPlayingSoundId = -1;
    private int[] preloadedSounds; // 预加载的音效ID数组
    
    // 游戏网格和方块
    private int[][] gameGrid; // 存储方块类型，0表示空，1-7表示不同类型
    private int[][] currentBlock;
    private int[][] nextBlock;
    private int currentX, currentY;
    private int blockType;
    private int nextBlockType;
    
    // UI组件
    private GridLayout gameGridLayout;
    private GridLayout nextBlockPreview;
    private TextView scoreText;
    private Button backBtn;
    private Button rotateBtn;
    private Button leftBtn;
    private Button downBtn;
    private Button rightBtn;
    private Button fastDropBtn;
    
    // 游戏循环
    private Handler handler;
    private Runnable gameLoop;
    private Runnable timerLoop;
    
    // 方块形状定义（7种俄罗斯方块形状，使用二维数组表示）
    private final int[][][] BLOCKS = {
            // I形
            {
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
            },
            // O形
            {
                {1, 1},
                {1, 1}
            },
            // T形
            {
                {0, 1, 0},
                {1, 1, 1},
                {0, 0, 0}
            },
            // L形
            {
                {0, 0, 1},
                {1, 1, 1},
                {0, 0, 0}
            },
            // J形
            {
                {1, 0, 0},
                {1, 1, 1},
                {0, 0, 0}
            },
            // S形
            {
                {0, 1, 1},
                {1, 1, 0},
                {0, 0, 0}
            },
            // Z形
            {
                {1, 1, 0},
                {0, 1, 1},
                {0, 0, 0}
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        
        // 停止音乐服务，游戏界面不播放背景音乐
        stopService(new Intent(this, MusicService.class));
        
        // 初始化游戏
        initGame();
        
        // 初始化UI
        initUI();
        
        // 初始化游戏视图
        initGameViews();
        
        // 开始游戏
        startGame();
    }
    
    /**
     * 初始化游戏数据
     */
    private void initGame() {
        // 初始化游戏网格
        gameGrid = new int[GRID_HEIGHT][GRID_WIDTH];
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                gameGrid[row][col] = 0; // 0表示空
            }
        }
        
        // 初始化方块
        generateBlocks();
        
        // 初始化分数
        score = 0;
        
        // 从SharedPreferences获取设置
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        isControllerEnabled = sharedPreferences.getBoolean("controller_enabled", false);
        isGameSoundEnabled = sharedPreferences.getBoolean("game_sound_enabled", true);
        
        // 初始化SoundManager
        SoundManager.getInstance().init(this, isGameSoundEnabled);
        
        // 初始化音效播放
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new android.media.SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(10) // 增加最大流数量以支持更多音效
                    .build();
        } else {
            soundPool = new android.media.SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
        }
        
        // 预加载所有方块音效
        preloadedSounds = new int[7];
        preloadedSounds[0] = soundPool.load(this, BLOCK_SOUNDS[0], 1); // I型 - 鸡蛋
        preloadedSounds[1] = soundPool.load(this, BLOCK_SOUNDS[1], 1); // O型 - 白菜
        preloadedSounds[2] = soundPool.load(this, BLOCK_SOUNDS[2], 1); // T型 - 烟头
        preloadedSounds[3] = soundPool.load(this, BLOCK_SOUNDS[3], 1); // L型 - 粑粑
        preloadedSounds[4] = soundPool.load(this, BLOCK_SOUNDS[4], 1); // J型 - 粑粑
        preloadedSounds[5] = soundPool.load(this, BLOCK_SOUNDS[5], 1); // S型 - 石头
        preloadedSounds[6] = soundPool.load(this, BLOCK_SOUNDS[6], 1); // Z型 - 石头
        
        // 为第一个方块音效添加加载完成监听器
        soundPool.setOnLoadCompleteListener(new android.media.SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(android.media.SoundPool soundPool, int sampleId, int status) {
                // 只在游戏开始时播放第一个方块的音效
                if (sampleId == preloadedSounds[blockType] && status == 0 && isGameSoundEnabled) {
                    // 确保当前没有正在播放的音效
                    if (currentPlayingSoundId != -1) {
                        soundPool.stop(currentPlayingSoundId);
                        currentPlayingSoundId = -1;
                    }
                    // 播放第一个方块的音效
                    currentPlayingSoundId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
        });
        
        // 初始化游戏循环
        handler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    moveDown();
                    handler.postDelayed(this, GAME_SPEED);
                }
            }
        };
        
        // 初始化计时器循环
        timerLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    gameDuration++;
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }
    
    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 获取UI组件
        gameGridLayout = findViewById(R.id.game_grid);
        nextBlockPreview = findViewById(R.id.next_block_preview);
        scoreText = findViewById(R.id.score_text);
        backBtn = findViewById(R.id.back_btn);
        rotateBtn = findViewById(R.id.rotate_btn);
        leftBtn = findViewById(R.id.left_btn);
        downBtn = findViewById(R.id.down_btn);
        rightBtn = findViewById(R.id.right_btn);
        fastDropBtn = findViewById(R.id.fast_drop_btn);
        
        // 设置按钮点击事件
        setupButtonListeners();
        
        // 更新分数显示
        updateScore();
        
        // 更新按钮文本，添加控制器映射
        updateButtonTextWithControllerMapping();
        
        // 检测控制器连接状态
        checkControllerConnection();
    }
    
    /**
     * 播放点击音效
     */
    private void playClickSound() {
        SoundManager.getInstance().playValidClickSound();
    }
    
    /**
     * 重置所有按钮背景为默认样式
     */
    private void resetButtonBackgrounds() {
        backBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        rotateBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        leftBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        downBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        rightBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        fastDropBtn.setBackgroundResource(R.drawable.btn_white_black_border);
    }
    
    /**
     * 将指定按钮设置为高亮样式
     * @param button 要高亮的按钮
     */
    private void setButtonHighlighted(Button button) {
        button.setBackgroundResource(R.drawable.btn_blue_border);
    }
    
    /**
     * 设置按钮监听器
     */
    private void setupButtonListeners() {
        backBtn.setOnClickListener(v -> {
            playClickSound();
            onBackPressed();
        });
        rotateBtn.setOnClickListener(v -> {
            playClickSound();
            rotateBlock();
        });
        leftBtn.setOnClickListener(v -> {
            playClickSound();
            moveLeft();
        });
        downBtn.setOnClickListener(v -> {
            playClickSound();
            moveDown();
        });
        rightBtn.setOnClickListener(v -> {
            playClickSound();
            moveRight();
        });
        fastDropBtn.setOnClickListener(v -> {
            playClickSound();
            fastDrop();
        });
    }
    
    /**
     * 初始化游戏视图
     */
    private void initGameViews() {
        // 初始化游戏网格视图
        initGameGridView();
        
        // 初始化下一个方块预览视图
        initNextBlockPreview();
    }
    
    /**
     * 初始化游戏网格视图
     */
    private void initGameGridView() {
        gameGridLayout.removeAllViews();
        gameGridLayout.setColumnCount(GRID_WIDTH);
        gameGridLayout.setRowCount(GRID_HEIGHT);
        
        // 创建游戏网格的ImageView
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                ImageView cell = createGridCell();
                gameGridLayout.addView(cell);
            }
        }
    }
    
    /**
     * 初始化下一个方块预览视图
     */
    private void initNextBlockPreview() {
        nextBlockPreview.removeAllViews();
        nextBlockPreview.setColumnCount(4);
        nextBlockPreview.setRowCount(4);
        
        // 创建下一个方块预览的ImageView
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                ImageView cell = createGridCell();
                nextBlockPreview.addView(cell);
            }
        }
    }
    
    /**
     * 创建网格单元
     */
    private ImageView createGridCell() {
        ImageView cell = new ImageView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = DEFAULT_BLOCK_SIZE;
        params.height = DEFAULT_BLOCK_SIZE;
        params.setMargins(1, 1, 1, 1); // 添加边距来显示网格线
        cell.setBackgroundResource(R.color.white); // 白色背景
        cell.setLayoutParams(params);
        return cell;
    }
    
    /**
     * 生成方块
     */
    private void generateBlocks() {
        // 初始化当前方块和下一个方块
        blockType = (int) (Math.random() * BLOCKS.length);
        nextBlockType = (int) (Math.random() * BLOCKS.length);
        currentBlock = BLOCKS[blockType];
        nextBlock = BLOCKS[nextBlockType];
        
        // 设置初始位置
        currentX = GRID_WIDTH / 2 - currentBlock[0].length / 2;
        currentY = 0;
    }
    
    /**
     * 开始游戏
     */
    private void startGame() {
        isGameRunning = true;
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;
        renderGameGrid();
        renderNextBlock();
        // 第一个方块的音效会在音效预加载完成后自动播放
        handler.postDelayed(gameLoop, GAME_SPEED);
        handler.postDelayed(timerLoop, 1000);
    }
    
    /**
     * 渲染游戏网格
     */
    private void renderGameGrid() {
        // 清空所有方块
        clearGameGrid();
        
        // 绘制固定的方块
        drawFixedBlocks();
        
        // 绘制当前移动的方块
        drawCurrentBlock();
    }
    
    /**
     * 清空游戏网格
     */
    private void clearGameGrid() {
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                int index = row * GRID_WIDTH + col;
                ImageView cell = (ImageView) gameGridLayout.getChildAt(index);
                if (cell != null) {
                    cell.setImageResource(0);
                }
            }
        }
    }
    
    /**
     * 绘制固定的方块
     */
    private void drawFixedBlocks() {
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                int blockType = gameGrid[row][col];
                if (blockType != 0) {
                    int index = row * GRID_WIDTH + col;
                    ImageView cell = (ImageView) gameGridLayout.getChildAt(index);
                    if (cell != null) {
                        // 根据存储的方块类型显示对应的图标
                        cell.setImageResource(BLOCK_ICONS[blockType - 1]);
                    }
                }
            }
        }
    }
    
    /**
     * 绘制当前移动的方块
     */
    private void drawCurrentBlock() {
        int iconResId = BLOCK_ICONS[blockType];
        for (int row = 0; row < currentBlock.length; row++) {
            for (int col = 0; col < currentBlock[row].length; col++) {
                if (currentBlock[row][col] == 1) {
                    int gridX = currentX + col;
                    int gridY = currentY + row;
                    if (gridX >= 0 && gridX < GRID_WIDTH && gridY >= 0 && gridY < GRID_HEIGHT) {
                        int index = gridY * GRID_WIDTH + gridX;
                        ImageView cell = (ImageView) gameGridLayout.getChildAt(index);
                        if (cell != null) {
                            cell.setImageResource(iconResId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 渲染下一个方块预览
     */
    private void renderNextBlock() {
        // 清空预览区域
        clearNextBlockPreview();
        
        // 绘制下一个方块
        drawNextBlock();
    }
    
    /**
     * 清空下一个方块预览
     */
    private void clearNextBlockPreview() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                ImageView cell = (ImageView) nextBlockPreview.getChildAt(index);
                if (cell != null) {
                    cell.setImageResource(0);
                }
            }
        }
    }
    
    /**
     * 绘制下一个方块
     */
    private void drawNextBlock() {
        int iconResId = BLOCK_ICONS[nextBlockType];
        
        // 清空预览区域
        clearNextBlockPreview();
        
        // 根据方块类型，为每个方块计算精确的居中位置
        int offsetX = 0;
        int offsetY = 0;
        
        // 遍历方块矩阵，直接绘制到预览区域的正确位置
        switch (nextBlockType) {
            case 0: // I型 - 长条 (4x4矩阵)
                // 绘制在第1行（索引从0开始），第0-3列
                setPreviewCell(1, 0, iconResId);
                setPreviewCell(1, 1, iconResId);
                setPreviewCell(1, 2, iconResId);
                setPreviewCell(1, 3, iconResId);
                return;
            
            case 1: // O型 - 正方形 (2x2矩阵)
                // 绘制在第1-2行，第1-2列
                setPreviewCell(1, 1, iconResId);
                setPreviewCell(1, 2, iconResId);
                setPreviewCell(2, 1, iconResId);
                setPreviewCell(2, 2, iconResId);
                return;
            
            case 2: // T型 (3x3矩阵)
                // 绘制在第1-2行，第1-3列
                setPreviewCell(1, 2, iconResId);
                setPreviewCell(2, 1, iconResId);
                setPreviewCell(2, 2, iconResId);
                setPreviewCell(2, 3, iconResId);
                return;
            
            case 3: // L型 (3x3矩阵)
                // 绘制在第1-2行，第1-3列
                setPreviewCell(1, 3, iconResId);
                setPreviewCell(2, 1, iconResId);
                setPreviewCell(2, 2, iconResId);
                setPreviewCell(2, 3, iconResId);
                return;
            
            case 4: // J型 (3x3矩阵)
                // 绘制在第1-2行，第1-3列
                setPreviewCell(1, 1, iconResId);
                setPreviewCell(2, 1, iconResId);
                setPreviewCell(2, 2, iconResId);
                setPreviewCell(2, 3, iconResId);
                return;
            
            case 5: // S型 (3x3矩阵)
                // 绘制在第1-2行，第1-3列
                setPreviewCell(1, 2, iconResId);
                setPreviewCell(1, 3, iconResId);
                setPreviewCell(2, 1, iconResId);
                setPreviewCell(2, 2, iconResId);
                return;
            
            case 6: // Z型 (3x3矩阵)
                // 绘制在第1-2行，第1-3列
                setPreviewCell(1, 1, iconResId);
                setPreviewCell(1, 2, iconResId);
                setPreviewCell(2, 2, iconResId);
                setPreviewCell(2, 3, iconResId);
                return;
        }
    }
    
    /**
     * 设置预览区域的单个单元格
     * @param row 行索引（0-3）
     * @param col 列索引（0-3）
     * @param resId 资源ID
     */
    private void setPreviewCell(int row, int col, int resId) {
        if (row >= 0 && row < 4 && col >= 0 && col < 4) {
            int index = row * 4 + col;
            ImageView cell = (ImageView) nextBlockPreview.getChildAt(index);
            if (cell != null) {
                cell.setImageResource(resId);
            }
        }
    }
    
    /**
     * 更新分数
     */
    private void updateScore() {
        scoreText.setText("得分: " + score);
    }
    
    /**
     * 检查碰撞
     */
    private boolean checkCollision(int x, int y, int[][] block) {
        for (int row = 0; row < block.length; row++) {
            for (int col = 0; col < block[row].length; col++) {
                if (block[row][col] == 1) {
                    int newX = x + col;
                    int newY = y + row;
                    if (newX < 0 || newX >= GRID_WIDTH || newY >= GRID_HEIGHT) {
                        return true; // 边界碰撞
                    }
                    if (newY >= 0 && gameGrid[newY][newX] != 0) {
                        return true; // 方块碰撞
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 旋转方块
     */
    private void rotateBlock() {
        int[][] rotatedBlock = rotateMatrix(currentBlock);
        if (!checkCollision(currentX, currentY, rotatedBlock)) {
            currentBlock = rotatedBlock;
            renderGameGrid();
        }
    }
    
    /**
     * 旋转矩阵
     */
    private int[][] rotateMatrix(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = matrix[i][j];
            }
        }
        
        return rotated;
    }
    
    /**
     * 向左移动方块
     */
    private void moveLeft() {
        if (!checkCollision(currentX - 1, currentY, currentBlock)) {
            currentX--;
            renderGameGrid();
        }
    }
    
    /**
     * 向右移动方块
     */
    private void moveRight() {
        if (!checkCollision(currentX + 1, currentY, currentBlock)) {
            currentX++;
            renderGameGrid();
        }
    }
    
    /**
     * 向下移动方块
     */
    private void moveDown() {
        if (!checkCollision(currentX, currentY + 1, currentBlock)) {
            currentY++;
            renderGameGrid();
        } else {
            // 方块无法继续下落，固定到网格上
            fixBlock();
            // 检查是否有完整的行需要消除
            checkLines();
            // 生成新的方块
            spawnNewBlock();
        }
    }
    
    /**
     * 快速下落
     */
    private void fastDrop() {
        while (!checkCollision(currentX, currentY + 1, currentBlock)) {
            currentY++;
            renderGameGrid();
        }
        // 固定方块
        fixBlock();
        // 检查行
        checkLines();
        // 生成新方块
        spawnNewBlock();
    }
    
    /**
     * 将当前方块固定到游戏网格上
     */
    private void fixBlock() {
        for (int row = 0; row < currentBlock.length; row++) {
            for (int col = 0; col < currentBlock[row].length; col++) {
                if (currentBlock[row][col] == 1) {
                    int gridX = currentX + col;
                    int gridY = currentY + row;
                    if (gridX >= 0 && gridX < GRID_WIDTH && gridY >= 0 && gridY < GRID_HEIGHT) {
                        gameGrid[gridY][gridX] = blockType + 1; // 存储方块类型（1-7）
                    }
                }
            }
        }
        // 方块落下固定+5分
        score += 5;
        updateScore();
    }
    
    /**
     * 检查并消除完整的行
     */
    private void checkLines() {
        int linesCleared = 0;
        for (int row = GRID_HEIGHT - 1; row >= 0; row--) {
            if (isLineFull(row)) {
                clearLine(row);
                linesCleared++;
                row++; // 重新检查当前行
            }
        }
        
        // 根据消除的行数增加分数
        if (linesCleared > 0) {
            // 积分规则：一消+10，二消+30，三消+60，四消+100
            switch (linesCleared) {
                case 1:
                    score += 10;
                    break;
                case 2:
                    score += 30;
                    break;
                case 3:
                    score += 60;
                    break;
                case 4:
                    score += 100;
                    break;
                default:
                    score += linesCleared * 100; // 超过四行按原来的规则
                    break;
            }
            updateScore();
        }
    }
    
    /**
     * 检查行是否满了
     */
    private boolean isLineFull(int row) {
        for (int col = 0; col < GRID_WIDTH; col++) {
            if (gameGrid[row][col] == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 消除指定行
     */
    private void clearLine(int line) {
        // 将上面的行下移
        for (int row = line; row > 0; row--) {
            System.arraycopy(gameGrid[row - 1], 0, gameGrid[row], 0, GRID_WIDTH);
        }
        // 清空第一行
        for (int col = 0; col < GRID_WIDTH; col++) {
            gameGrid[0][col] = 0;
        }
        // 重新渲染
        renderGameGrid();
    }
    
    /**
     * 生成新的方块
     */
    private void spawnNewBlock() {
        // 使用下一个方块作为当前方块
        blockType = nextBlockType;
        currentBlock = BLOCKS[blockType];
        currentX = GRID_WIDTH / 2 - currentBlock[0].length / 2;
        currentY = 0;
        
        // 生成新的下一个方块
        nextBlockType = (int) (Math.random() * BLOCKS.length);
        nextBlock = BLOCKS[nextBlockType];
        
        // 检查游戏是否结束
        if (checkCollision(currentX, currentY, currentBlock)) {
            endGame();
            return;
        }
        
        // 更新UI
        renderGameGrid();
        renderNextBlock();
        
        // 播放方块生成音效
        playBlockSpawnSound();
    }
    
    /**
     * 播放方块生成音效
     */
    private void playBlockSpawnSound() {
        if (!isGameSoundEnabled || soundPool == null) {
            return;
        }
        
        // 停止当前正在播放的音效
        if (currentPlayingSoundId != -1) {
            soundPool.stop(currentPlayingSoundId);
            currentPlayingSoundId = -1;
        }
        
        // 直接播放预加载的音效
        currentPlayingSoundId = soundPool.play(preloadedSounds[blockType], 1.0f, 1.0f, 0, 0, 1.0f);
    }
    

    
    /**
     * 游戏结束
     */
    private void endGame() {
        isGameRunning = false;
        handler.removeCallbacks(gameLoop);
        handler.removeCallbacks(timerLoop);
        
        // 计算游戏时长
        if (gameStartTime > 0) {
            gameDuration = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
        }
        
        // 播放游戏结束音效
        playGameOverSound();
        
        // 显示游戏结束弹窗
        showGameOverDialog();
    }
    
    /**
     * 播放游戏结束音效
     */
    private void playGameOverSound() {
        if (!isGameSoundEnabled || soundPool == null) {
            return;
        }
        
        // 停止当前正在播放的音效
        if (currentPlayingSoundId != -1) {
            soundPool.stop(currentPlayingSoundId);
        }
        
        // 加载并播放游戏结束音效
        int soundId = soundPool.load(this, GAME_OVER_SOUND, 1);
        
        // 音效加载完成后播放
        soundPool.setOnLoadCompleteListener(new android.media.SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(android.media.SoundPool soundPool, int sampleId, int status) {
                if (status == 0 && isGameSoundEnabled) {
                    currentPlayingSoundId = soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
        });
    }
    
    /**
     * 显示游戏结束弹窗
     */
    private void showGameOverDialog() {
        // 使用普通的Dialog而不是AlertDialog，避免样式冲突
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setCancelable(false);
        
        // 设置对话框主题为透明背景
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        // 自定义对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.game_over_dialog, null);
        dialog.setContentView(dialogView);
        
        // 格式化游戏时长为 MM:SS 格式
        String formattedTime = String.format("%02d:%02d", gameDuration / 60, gameDuration % 60);
        
        // 绑定控件
        TextView titleText = dialogView.findViewById(R.id.dialog_title);
        TextView scoreText = dialogView.findViewById(R.id.dialog_score);
        TextView timeText = dialogView.findViewById(R.id.dialog_time);
        Button restartBtn = dialogView.findViewById(R.id.restart_btn);
        Button saveBtn = dialogView.findViewById(R.id.save_btn);
        Button homeBtn = dialogView.findViewById(R.id.home_btn);
        
        // 显式设置按钮样式，确保覆盖任何主题样式
        restartBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        restartBtn.setTextColor(getResources().getColor(R.color.black));
        restartBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        
        saveBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        saveBtn.setTextColor(getResources().getColor(R.color.black));
        saveBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        
        homeBtn.setBackgroundResource(R.drawable.btn_white_black_border);
        homeBtn.setTextColor(getResources().getColor(R.color.black));
        homeBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // 设置内容
        titleText.setText("游戏结束");
        scoreText.setText(String.format("得分: %d", score));
        timeText.setText(String.format("游戏时长: %s", formattedTime));
        
        // 设置按钮点击事件
        restartBtn.setOnClickListener(v -> {
            // 播放点击音效
            SoundManager.getInstance().playValidClickSound();
            dialog.dismiss();
            restartGame();
        });
        
        saveBtn.setOnClickListener(v -> {
            // 播放点击音效
            SoundManager.getInstance().playValidClickSound();
            dialog.dismiss();
            saveGameRecord();
            onBackPressed();
        });
        
        homeBtn.setOnClickListener(v -> {
            // 播放点击音效
            SoundManager.getInstance().playValidClickSound();
            dialog.dismiss();
            onBackPressed();
        });
        
        // 为弹窗按钮添加焦点监听，实现手柄导航高亮和音效
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 获得焦点，显示蓝色边框
                    v.setBackgroundResource(R.drawable.btn_blue_border);
                    // 播放点击音效，提供听觉反馈
                    SoundManager.getInstance().playValidClickSound();
                } else {
                    // 失去焦点，恢复默认样式
                    v.setBackgroundResource(R.drawable.btn_white_black_border);
                }
            }
        };
        
        // 为所有弹窗按钮添加焦点监听
        restartBtn.setOnFocusChangeListener(focusChangeListener);
        saveBtn.setOnFocusChangeListener(focusChangeListener);
        homeBtn.setOnFocusChangeListener(focusChangeListener);
        
        // 设置初始焦点为重新开始按钮
        restartBtn.requestFocus();
        
        // 显示对话框
        dialog.show();
    }
    
    /**
     * 重新开始游戏
     */
    private void restartGame() {
        // 重置游戏数据
        initGame();
        initGameViews();
        updateScore();
        startGame();
    }
    
    /**
     * 保存游戏记录
     */
    private void saveGameRecord() {
        // 格式化游戏时长为 MM:SS 格式
        String formattedTime = String.format("%02d:%02d", gameDuration / 60, gameDuration % 60);
        
        // 格式化日期
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = dateFormat.format(new java.util.Date());
        
        // 保存记录
        GameRecordActivity.saveRecord(this, score, formattedTime, currentDate);
    }
    
    /**
     * 返回主菜单
     */
    @Override
    public void onBackPressed() {
        isGameRunning = false;
        handler.removeCallbacks(gameLoop);
        super.onBackPressed();
        // 检查并应用转场动画
        applyTransitionAnimation();
    }
    
    /**
     * 检查并应用转场动画
     */
    private void applyTransitionAnimation() {
        // 从SharedPreferences获取动画设置
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE);
        boolean isAnimationEnabled = sharedPreferences.getBoolean("animation_enabled", false);
        if (isAnimationEnabled) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            // 明确指定无动画
            overridePendingTransition(0, 0);
        }
    }
    
    /**
     * 生命周期管理
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameRunning) {
            isGameRunning = true;
            startGame();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isGameRunning = false;
        handler.removeCallbacks(gameLoop);
        // 离开游戏界面时停止音乐服务
        stopService(new Intent(this, MusicService.class));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
        handler.removeCallbacks(gameLoop);
        
        // 释放音效资源
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        // 游戏界面销毁时，不需要停止音乐服务
        // 因为回到主菜单时，主菜单会重新启动音乐服务
        // 这样可以实现从游戏界面回到主菜单时，音乐无缝继续播放
    }
    
    /**
     * 更新按钮文本，添加控制器映射
     */
    private void updateButtonTextWithControllerMapping() {
        if (isControllerEnabled) {
            // 添加控制器映射文本
            leftBtn.setText("左移\n(←/X/L1/L2)");
            rightBtn.setText("右移\n(→/Y/R1/R2)");
            downBtn.setText("下移\n(↓)");
            rotateBtn.setText("旋转\n(↑/A)");
            fastDropBtn.setText("快速下落\n(B)");
        } else {
            // 恢复默认文本
            leftBtn.setText("左移");
            rightBtn.setText("右移");
            downBtn.setText("下移");
            rotateBtn.setText("旋转");
            fastDropBtn.setText("快速下落");
        }
    }
    
    /**
     * 检测控制器连接状态
     */
    private void checkControllerConnection() {
        // 检测到控制器按键时会自动触发弹窗
    }
    
    /**
     * 显示控制器连接弹窗
     */
    private void showControllerConnectedDialog() {
        // 使用普通的Dialog显示控制器连接提示
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setCancelable(true);
        
        // 设置对话框主题为透明背景
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        // 自定义对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.controller_connected_dialog, null);
        dialog.setContentView(dialogView);
        
        // 绑定控件
        Button closeBtn = dialogView.findViewById(R.id.close_btn);
        
        // 设置按钮点击事件
        closeBtn.setOnClickListener(v -> {
            dialog.dismiss();
        });
        
        // 显示对话框
        dialog.show();
    }
    
    /**
     * 处理按键按下事件
     */
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (!isGameRunning || !isControllerEnabled) {
            return super.onKeyDown(keyCode, event);
        }
        
        // 如果是第一次检测到控制器按键，显示连接弹窗
        boolean isFirstControllerInput = false;
        if (!isControllerConnected) {
            isControllerConnected = true;
            isFirstControllerInput = true;
        }
        
        // 重置所有按钮背景
        resetButtonBackgrounds();
        
        // 手柄按键映射
        switch (keyCode) {
            // D-pad方向键
            case android.view.KeyEvent.KEYCODE_DPAD_LEFT:
                playClickSound();
                moveLeft();
                setButtonHighlighted(leftBtn);
                break;
            case android.view.KeyEvent.KEYCODE_DPAD_RIGHT:
                playClickSound();
                moveRight();
                setButtonHighlighted(rightBtn);
                break;
            case android.view.KeyEvent.KEYCODE_DPAD_DOWN:
                playClickSound();
                moveDown();
                setButtonHighlighted(downBtn);
                break;
            case android.view.KeyEvent.KEYCODE_DPAD_UP:
                playClickSound();
                rotateBlock();
                setButtonHighlighted(rotateBtn);
                break;
                
            // 标准按钮
            case android.view.KeyEvent.KEYCODE_BUTTON_A: // A键 - 旋转
                playClickSound();
                rotateBlock();
                setButtonHighlighted(rotateBtn);
                break;
            case android.view.KeyEvent.KEYCODE_BUTTON_B: // B键 - 快速下落
                playClickSound();
                fastDrop();
                setButtonHighlighted(fastDropBtn);
                break;
            case android.view.KeyEvent.KEYCODE_BUTTON_X: // X键 - 左移
                playClickSound();
                moveLeft();
                setButtonHighlighted(leftBtn);
                break;
            case android.view.KeyEvent.KEYCODE_BUTTON_Y: // Y键 - 右移
                playClickSound();
                moveRight();
                setButtonHighlighted(rightBtn);
                break;
                
            // 肩键
            case android.view.KeyEvent.KEYCODE_BUTTON_L1: // L1 - 左移
            case android.view.KeyEvent.KEYCODE_BUTTON_L2: // L2 - 左移
                playClickSound();
                moveLeft();
                setButtonHighlighted(leftBtn);
                break;
            case android.view.KeyEvent.KEYCODE_BUTTON_R1: // R1 - 右移
            case android.view.KeyEvent.KEYCODE_BUTTON_R2: // R2 - 右移
                playClickSound();
                moveRight();
                setButtonHighlighted(rightBtn);
                break;
                
            default:
                // 播放无效操作音效
                SoundManager.getInstance().playInvalidOperationSound();
                return super.onKeyDown(keyCode, event);
        }
        
        // 如果是第一次检测到控制器输入，显示连接弹窗
        if (isFirstControllerInput) {
            showControllerConnectedDialog();
        }
        
        return true;
    }
    
    /**
     * 处理按键释放事件
     */
    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (!isGameRunning || !isControllerEnabled) {
            return super.onKeyUp(keyCode, event);
        }
        
        // 重置所有按钮背景
        resetButtonBackgrounds();
        
        return super.onKeyUp(keyCode, event);
    }
}

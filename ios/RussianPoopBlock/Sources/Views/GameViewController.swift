import UIKit
import GameController

class GameViewController: UIViewController {

    private let gridWidth = 10
    private let gridHeight = 20
    private let blockSize: CGFloat = 30

    private var gameEngine: TetrisGameEngine!
    private var gridCells: [[UIImageView]] = []
    private var nextBlockCells: [[UIImageView]] = []

    private var highlightedButton: UIButton?
    private var isControllerConnected = false
    private var isControllerEnabled = false

    private let scoreLabel: UILabel = {
        let label = UILabel()
        label.text = "得分: 0"
        label.font = UIFont.boldSystemFont(ofSize: 22)
        label.textColor = .black
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let nextBlockTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "下一个方块"
        label.font = UIFont.boldSystemFont(ofSize: 20)
        label.textColor = .black
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let gameGridContainer: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.borderWidth = 2
        view.layer.borderColor = UIColor.black.cgColor
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

    private let nextBlockContainer: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#FFD959")
        view.layer.borderWidth = 2
        view.layer.borderColor = UIColor.black.cgColor
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

    private let nextBlockGridView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#CCCCCC")
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

    private lazy var rotateBtn = createControlButton(title: "旋转")
    private lazy var leftBtn = createControlButton(title: "左移")
    private lazy var rightBtn = createControlButton(title: "右移")
    private lazy var downBtn = createControlButton(title: "下移")
    private lazy var fastDropBtn = createControlButton(title: "快速下落")
    private lazy var backBtn: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("返回主菜单", for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 14)
        button.setTitleColor(.black, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 8
        button.layer.borderWidth = 2
        button.layer.borderColor = UIColor.black.cgColor
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupGameEngine()
        setupActions()
        setupGamepad()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        gameEngine.startGame()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        gameEngine.stopGame()
    }

    private func setupUI() {
        view.backgroundColor = UIColor(hex: "#FFD959")

        view.addSubview(backBtn)
        view.addSubview(gameGridContainer)
        view.addSubview(nextBlockContainer)
        view.addSubview(scoreLabel)
        view.addSubview(nextBlockTitleLabel)
        nextBlockContainer.addSubview(nextBlockGridView)

        setupGridCells()
        setupNextBlockCells()

        let controlsStack = UIStackView(arrangedSubviews: [
            createControlRow([rotateBtn]),
            createControlRow([leftBtn, rightBtn]),
            createControlRow([downBtn])
        ])
        controlsStack.axis = .vertical
        controlsStack.spacing = 15
        controlsStack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(controlsStack)

        view.addSubview(fastDropBtn)

        NSLayoutConstraint.activate([
            backBtn.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 15),
            backBtn.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 15),
            backBtn.widthAnchor.constraint(equalToConstant: 120),
            backBtn.heightAnchor.constraint(equalToConstant: 50),

            scoreLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 15),
            scoreLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),

            nextBlockTitleLabel.topAnchor.constraint(equalTo: scoreLabel.bottomAnchor, constant: 15),
            nextBlockTitleLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),

            nextBlockContainer.topAnchor.constraint(equalTo: nextBlockTitleLabel.bottomAnchor, constant: 12),
            nextBlockContainer.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            nextBlockContainer.widthAnchor.constraint(equalToConstant: 140),
            nextBlockContainer.heightAnchor.constraint(equalToConstant: 140),

            nextBlockGridView.centerXAnchor.constraint(equalTo: nextBlockContainer.centerXAnchor),
            nextBlockGridView.centerYAnchor.constraint(equalTo: nextBlockContainer.centerYAnchor),
            nextBlockGridView.widthAnchor.constraint(equalToConstant: 120),
            nextBlockGridView.heightAnchor.constraint(equalToConstant: 120),

            gameGridContainer.topAnchor.constraint(equalTo: nextBlockContainer.bottomAnchor, constant: 20),
            gameGridContainer.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),

            controlsStack.topAnchor.constraint(equalTo: gameGridContainer.topAnchor),
            controlsStack.leadingAnchor.constraint(equalTo: gameGridContainer.trailingAnchor, constant: 20),
            controlsStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),

            fastDropBtn.topAnchor.constraint(equalTo: controlsStack.bottomAnchor, constant: 20),
            fastDropBtn.leadingAnchor.constraint(equalTo: gameGridContainer.trailingAnchor, constant: 20),
            fastDropBtn.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10),
            fastDropBtn.heightAnchor.constraint(equalToConstant: 60)
        ])
    }

    private func setupGridCells() {
        gridCells = []

        for row in 0..<gridHeight {
            var rowCells: [UIImageView] = []
            for col in 0..<gridWidth {
                let cell = UIImageView()
                cell.backgroundColor = .white
                cell.frame = CGRect(x: CGFloat(col) * blockSize, y: CGFloat(row) * blockSize, width: blockSize, height: blockSize)
                gameGridContainer.addSubview(cell)
                rowCells.append(cell)
            }
            gridCells.append(rowCells)
        }

        gameGridContainer.widthAnchor.constraint(equalToConstant: CGFloat(gridWidth) * blockSize + 4).isActive = true
        gameGridContainer.heightAnchor.constraint(equalToConstant: CGFloat(gridHeight) * blockSize + 4).isActive = true
    }

    private func setupNextBlockCells() {
        nextBlockCells = []

        for row in 0..<4 {
            var rowCells: [UIImageView] = []
            for col in 0..<4 {
                let cell = UIImageView()
                cell.backgroundColor = UIColor(hex: "#CCCCCC")
                cell.frame = CGRect(x: CGFloat(col) * 30, y: CGFloat(row) * 30, width: 30, height: 30)
                nextBlockGridView.addSubview(cell)
                rowCells.append(cell)
            }
            nextBlockCells.append(rowCells)
        }
    }

    private func createControlButton(title: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 18)
        button.setTitleColor(.black, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 8
        button.layer.borderWidth = 2
        button.layer.borderColor = UIColor.black.cgColor
        button.translatesAutoresizingMaskIntoConstraints = false
        button.widthAnchor.constraint(equalToConstant: 80).isActive = true
        button.heightAnchor.constraint(equalToConstant: 80).isActive = true
        return button
    }

    private func createControlRow(_ buttons: [UIButton]) -> UIStackView {
        let stack = UIStackView(arrangedSubviews: buttons)
        stack.axis = .horizontal
        stack.spacing = 20
        stack.alignment = .center
        stack.distribution = .equalCentering
        return stack
    }

    private func setupGameEngine() {
        gameEngine = TetrisGameEngine()

        gameEngine.onScoreChanged = { [weak self] score in
            self?.scoreLabel.text = "得分: \(score)"
        }

        gameEngine.onGameGridUpdated = { [weak self] in
            self?.renderGameGrid()
        }

        gameEngine.onNextBlockUpdated = { [weak self] _ in
            self?.renderNextBlock()
        }

        gameEngine.onGameOver = { [weak self] score, duration in
            self?.showGameOverDialog(score: score, duration: duration)
        }
    }

    private func setupActions() {
        backBtn.addTarget(self, action: #selector(backTapped), for: .touchUpInside)
        rotateBtn.addTarget(self, action: #selector(rotateTapped), for: .touchUpInside)
        leftBtn.addTarget(self, action: #selector(leftTapped), for: .touchUpInside)
        rightBtn.addTarget(self, action: #selector(rightTapped), for: .touchUpInside)
        downBtn.addTarget(self, action: #selector(downTapped), for: .touchUpInside)
        fastDropBtn.addTarget(self, action: #selector(fastDropTapped), for: .touchUpInside)
    }

    private func setupGamepad() {
        isControllerEnabled = GameSettings.shared.controllerEnabled

        if isControllerEnabled {
            updateButtonTextWithControllerMapping()
            checkGamepadConnection()
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(gamepadDidConnect),
                name: .GCControllerDidConnect,
                object: nil
            )
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(gamepadDidDisconnect),
                name: .GCControllerDidDisconnect,
                object: nil
            )
        }
    }

    @objc private func gamepadDidConnect(_ notification: Notification) {
        guard let controller = notification.object as? GCController else { return }

        if !isControllerConnected {
            isControllerConnected = true
            showControllerConnectedDialog()
        }

        setupControllerInputs(controller)
    }

    @objc private func gamepadDidDisconnect(_ notification: Notification) {
        isControllerConnected = false
    }

    private func checkGamepadConnection() {
        if let controller = GCController.controllers().first {
            isControllerConnected = true
            setupControllerInputs(controller)
        }
    }

    private func setupControllerInputs(_ controller: GCController) {
        if let gamepad = controller.extendedGamepad {
            gamepad.buttonA.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rotateTapped() }
            }
            gamepad.buttonB.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.fastDropTapped() }
            }
            gamepad.buttonX.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.leftTapped() }
            }
            gamepad.buttonY.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rightTapped() }
            }
            gamepad.dpad.left.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.leftTapped() }
            }
            gamepad.dpad.right.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rightTapped() }
            }
            gamepad.dpad.down.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.downTapped() }
            }
            gamepad.dpad.up.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rotateTapped() }
            }
            gamepad.leftShoulder.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.leftTapped() }
            }
            gamepad.rightShoulder.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rightTapped() }
            }
        }

        if let microGamepad = controller.microGamepad {
            microGamepad.buttonA.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rotateTapped() }
            }
            microGamepad.buttonX.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.fastDropTapped() }
            }
            microGamepad.dpad.up.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rotateTapped() }
            }
            microGamepad.dpad.down.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.downTapped() }
            }
            microGamepad.dpad.left.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.leftTapped() }
            }
            microGamepad.dpad.right.pressedChangedHandler = { [weak self] _, _, pressed in
                if pressed { self?.rightTapped() }
            }
        }
    }

    private func updateButtonTextWithControllerMapping() {
        rotateBtn.setTitle("旋转\n(↑/A)", for: .normal)
        leftBtn.setTitle("左移\n(←/X)", for: .normal)
        rightBtn.setTitle("右移\n(→/Y)", for: .normal)
        downBtn.setTitle("下移\n(↓)", for: .normal)
        fastDropBtn.setTitle("快速下落\n(B)", for: .normal)
    }

    private func showControllerConnectedDialog() {
        let alert = UIAlertController(
            title: "手柄已连接",
            message: "游戏手柄已连接，可以使用手柄操控游戏。\n\n按键说明：\n↑/A键：旋转\n↓键：下移\n←/X/L1/L2：左移\n→/Y/R1/R2：右移\nB/X键：快速下落",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }

    private func renderGameGrid() {
        for row in 0..<gridHeight {
            for col in 0..<gridWidth {
                let cellType = gameEngine.gameGrid[row][col]
                let cell = gridCells[row][col]

                if cellType == 0 {
                    cell.image = nil
                } else {
                    let iconName = gameEngine.blockIcons[cellType - 1]
                    cell.image = UIImage(named: iconName)
                }
            }
        }

        if let currentBlock = gameEngine.currentBlock {
            let iconName = gameEngine.blockIcons[currentBlock.type]
            for row in 0..<currentBlock.shape.count {
                for col in 0..<currentBlock.shape[row].count {
                    if currentBlock.shape[row][col] == 1 {
                        let gridX = currentBlock.x + col
                        let gridY = currentBlock.y + row
                        if gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight {
                            gridCells[gridY][gridX].image = UIImage(named: iconName)
                        }
                    }
                }
            }
        }
    }

    private func renderNextBlock() {
        for row in 0..<4 {
            for col in 0..<4 {
                nextBlockCells[row][col].image = nil
            }
        }

        let nextShape = gameEngine.getNextBlockShape()
        let iconName = gameEngine.blockIcons[gameEngine.nextBlockType]

        let offsets: [(Int, Int, Int, Int)] = [
            (1, 0, 1, 3),
            (1, 1, 2, 2),
            (1, 2, 2, 3),
            (1, 3, 2, 3),
            (1, 1, 2, 3),
            (1, 2, 2, 3),
            (1, 1, 2, 3)
        ]

        let offset = offsets[gameEngine.nextBlockType]

        for row in 0..<nextShape.count {
            for col in 0..<nextShape[row].count {
                if nextShape[row][col] == 1 {
                    let displayRow = row + offset.0 - 1
                    let displayCol = col + offset.1
                    if displayRow >= 0 && displayRow < 4 && displayCol >= 0 && displayCol < 4 {
                        nextBlockCells[displayRow][displayCol].image = UIImage(named: iconName)
                    }
                }
            }
        }
    }

    @objc private func backTapped() {
        SoundManager.shared.playValidClickSound()
        gameEngine.stopGame()
        MusicService.shared.startMusic()
        dismiss(animated: true)
    }

    @objc private func rotateTapped() {
        SoundManager.shared.playValidClickSound()
        highlightButton(rotateBtn)
        gameEngine.rotateBlock()
    }

    @objc private func leftTapped() {
        SoundManager.shared.playValidClickSound()
        highlightButton(leftBtn)
        gameEngine.moveLeft()
    }

    @objc private func rightTapped() {
        SoundManager.shared.playValidClickSound()
        highlightButton(rightBtn)
        gameEngine.moveRight()
    }

    @objc private func downTapped() {
        SoundManager.shared.playValidClickSound()
        highlightButton(downBtn)
        gameEngine.moveDown()
    }

    @objc private func fastDropTapped() {
        SoundManager.shared.playValidClickSound()
        highlightButton(fastDropBtn)
        gameEngine.fastDrop()
    }

    private func highlightButton(_ button: UIButton) {
        highlightedButton?.layer.borderColor = UIColor.black.cgColor
        highlightedButton?.layer.borderWidth = 2

        button.layer.borderColor = UIColor.systemBlue.cgColor
        button.layer.borderWidth = 3
        highlightedButton = button

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
            self?.highlightedButton?.layer.borderColor = UIColor.black.cgColor
            self?.highlightedButton?.layer.borderWidth = 2
        }
    }

    private func showGameOverDialog(score: Int, duration: Int) {
        let alert = UIAlertController(title: "游戏结束", message: "得分: \(score)\n游戏时长: \(formatDuration(duration))", preferredStyle: .alert)

        alert.addAction(UIAlertAction(title: "重新开始", style: .default) { [weak self] _ in
            SoundManager.shared.playValidClickSound()
            self?.gameEngine.startGame()
        })

        alert.addAction(UIAlertAction(title: "保存记录", style: .default) { [weak self] _ in
            SoundManager.shared.playValidClickSound()
            guard let self = self else { return }
            let record = GameRecord(score: score, time: self.formatDuration(duration), date: self.formatDate())
            GameRecordStorage.shared.saveRecord(record)
            self.gameEngine.startGame()
        })

        alert.addAction(UIAlertAction(title: "返回主菜单", style: .cancel) { [weak self] _ in
            SoundManager.shared.playValidClickSound()
            MusicService.shared.startMusic()
            self?.dismiss(animated: true)
        })

        present(alert, animated: true)
    }

    private func formatDuration(_ seconds: Int) -> String {
        let minutes = seconds / 60
        let secs = seconds % 60
        return String(format: "%02d:%02d", minutes, secs)
    }

    private func formatDate() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm"
        return formatter.string(from: Date())
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

import UIKit
import GameController

class GameViewController: UIViewController {

    private let gridWidth = 10
    private let gridHeight = 20
    private var blockSize: CGFloat = 15

    private var gameEngine: TetrisGameEngine!
    private var gridCells: [[UIView]] = []
    private var nextBlockCells: [[UIView]] = []

    private var highlightedButton: UIButton?
    private var isControllerConnected = false
    private var isControllerEnabled = false

    private let scoreLabel: UILabel = {
        let label = UILabel()
        label.text = "得分: 0"
        label.font = UIFont.boldSystemFont(ofSize: 20)
        label.textColor = .black
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let nextBlockTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "下一个"
        label.font = UIFont.boldSystemFont(ofSize: 16)
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
        view.backgroundColor = UIColor(hex: "#CCCCCC")
        view.layer.borderWidth = 2
        view.layer.borderColor = UIColor.black.cgColor
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()

    private let rotateBtn = UIButton(type: .system)
    private let leftBtn = UIButton(type: .system)
    private let rightBtn = UIButton(type: .system)
    private let downBtn = UIButton(type: .system)
    private let fastDropBtn = UIButton(type: .system)
    private let backBtn = UIButton(type: .system)

    override func viewDidLoad() {
        super.viewDidLoad()
        calculateBlockSize()
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

    private func calculateBlockSize() {
        let screenWidth = UIScreen.main.bounds.width
        let screenHeight = UIScreen.main.bounds.height
        let availableWidth = screenWidth - 140
        let availableHeight = screenHeight - 200
        let blockByWidth = availableWidth / CGFloat(gridWidth)
        let blockByHeight = availableHeight / CGFloat(gridHeight)
        blockSize = min(blockByWidth, blockByHeight, 25)
    }

    private func setupUI() {
        view.backgroundColor = UIColor(hex: "#FFD959")

        view.addSubview(backBtn)
        view.addSubview(scoreLabel)
        view.addSubview(nextBlockTitleLabel)
        view.addSubview(nextBlockContainer)
        view.addSubview(gameGridContainer)

        setupButton(backBtn, title: "返回", fontSize: 14)
        setupButton(rotateBtn, title: "旋转", fontSize: 16)
        setupButton(leftBtn, title: "◀ 左移", fontSize: 16)
        setupButton(rightBtn, title: "右移 ▶", fontSize: 16)
        setupButton(downBtn, title: "▼ 下移", fontSize: 16)
        setupButton(fastDropBtn, title: "快速下落", fontSize: 16)

        let topControlsStack = UIStackView(arrangedSubviews: [leftBtn, downBtn, rightBtn])
        topControlsStack.axis = .horizontal
        topControlsStack.spacing = 15
        topControlsStack.distribution = .fillEqually
        topControlsStack.translatesAutoresizingMaskIntoConstraints = false

        let controlsStack = UIStackView(arrangedSubviews: [rotateBtn, topControlsStack, fastDropBtn])
        controlsStack.axis = .vertical
        controlsStack.spacing = 12
        controlsStack.distribution = .fillEqually
        controlsStack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(controlsStack)

        setupGridCells()
        setupNextBlockCells()

        let gridWidthConstraint = CGFloat(gridWidth) * blockSize
        let gridHeightConstraint = CGFloat(gridHeight) * blockSize

        NSLayoutConstraint.activate([
            backBtn.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            backBtn.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 15),
            backBtn.widthAnchor.constraint(equalToConstant: 80),
            backBtn.heightAnchor.constraint(equalToConstant: 40),

            scoreLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            scoreLabel.leadingAnchor.constraint(equalTo: backBtn.trailingAnchor, constant: 20),

            nextBlockTitleLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 10),
            nextBlockTitleLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -15),

            nextBlockContainer.topAnchor.constraint(equalTo: nextBlockTitleLabel.bottomAnchor, constant: 8),
            nextBlockContainer.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -15),
            nextBlockContainer.widthAnchor.constraint(equalToConstant: 100),
            nextBlockContainer.heightAnchor.constraint(equalToConstant: 100),

            gameGridContainer.topAnchor.constraint(equalTo: backBtn.bottomAnchor, constant: 15),
            gameGridContainer.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            gameGridContainer.widthAnchor.constraint(equalToConstant: gridWidthConstraint),
            gameGridContainer.heightAnchor.constraint(equalToConstant: gridHeightConstraint),

            controlsStack.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -15),
            controlsStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 20),
            controlsStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -20),
            controlsStack.heightAnchor.constraint(equalToConstant: 200)
        ])
    }

    private func setupButton(_ button: UIButton, title: String, fontSize: CGFloat) {
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: fontSize)
        button.setTitleColor(.black, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 10
        button.layer.borderWidth = 2
        button.layer.borderColor = UIColor.black.cgColor
        button.translatesAutoresizingMaskIntoConstraints = false
    }

    private func setupGridCells() {
        for row in 0..<gridHeight {
            for col in 0..<gridWidth {
                let cell = UIView()
                cell.backgroundColor = .white
                cell.frame = CGRect(x: CGFloat(col) * blockSize, y: CGFloat(row) * blockSize, width: blockSize, height: blockSize)
                cell.layer.borderWidth = 0.5
                cell.layer.borderColor = UIColor.lightGray.cgColor
                gameGridContainer.addSubview(cell)
            }
        }
    }

    private func setupNextBlockCells() {
        let cellSize: CGFloat = 22
        for row in 0..<4 {
            for col in 0..<4 {
                let cell = UIView()
                cell.backgroundColor = UIColor(hex: "#CCCCCC")
                cell.frame = CGRect(x: 8 + CGFloat(col) * cellSize, y: 8 + CGFloat(row) * cellSize, width: cellSize, height: cellSize)
                cell.layer.borderWidth = 0.5
                cell.layer.borderColor = UIColor.darkGray.cgColor
                nextBlockContainer.addSubview(cell)
            }
        }
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

    private func showControllerConnectedDialog() {
        let alert = UIAlertController(
            title: "手柄已连接",
            message: "游戏手柄已连接，可以使用手柄操控游戏。",
            preferredStyle: .alert
        )
        alert.addAction(UIAlertAction(title: "确定", style: .default))
        present(alert, animated: true)
    }

    private func renderGameGrid() {
        let colors: [UIColor] = [
            UIColor(hex: "#FF6B6B"),
            UIColor(hex: "#4ECDC4"),
            UIColor(hex: "#45B7D1"),
            UIColor(hex: "#96CEB4"),
            UIColor(hex: "#FFEAA7"),
            UIColor(hex: "#DDA0DD"),
            UIColor(hex: "#98D8C8")
        ]

        for row in 0..<gridHeight {
            for col in 0..<gridWidth {
                let cellType = gameEngine.gameGrid[row][col]
                let index = row * gridWidth + col
                let cell = gameGridContainer.subviews[index]

                if cellType == 0 {
                    cell.backgroundColor = .white
                } else {
                    cell.backgroundColor = colors[cellType - 1]
                }
            }
        }

        if let currentBlock = gameEngine.currentBlock {
            let color = colors[currentBlock.type]
            for row in 0..<currentBlock.shape.count {
                for col in 0..<currentBlock.shape[row].count {
                    if currentBlock.shape[row][col] == 1 {
                        let gridX = currentBlock.x + col
                        let gridY = currentBlock.y + row
                        if gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight {
                            let index = gridY * gridWidth + gridX
                            gameGridContainer.subviews[index].backgroundColor = color
                        }
                    }
                }
            }
        }
    }

    private func renderNextBlock() {
        let colors: [UIColor] = [
            UIColor(hex: "#FF6B6B"),
            UIColor(hex: "#4ECDC4"),
            UIColor(hex: "#45B7D1"),
            UIColor(hex: "#96CEB4"),
            UIColor(hex: "#FFEAA7"),
            UIColor(hex: "#DDA0DD"),
            UIColor(hex: "#98D8C8")
        ]

        for i in 0..<16 {
            nextBlockContainer.subviews[i].backgroundColor = UIColor(hex: "#CCCCCC")
        }

        let nextShape = gameEngine.getNextBlockShape()
        let color = colors[gameEngine.nextBlockType]

        let offsets: [(Int, Int)] = [(1, 0), (1, 1), (1, 1), (1, 1), (1, 1), (1, 1), (1, 1)]
        let offset = offsets[gameEngine.nextBlockType]

        for row in 0..<nextShape.count {
            for col in 0..<nextShape[row].count {
                if nextShape[row][col] == 1 {
                    let displayRow = row + offset.0
                    let displayCol = col + offset.1
                    if displayRow >= 0 && displayRow < 4 && displayCol >= 0 && displayCol < 4 {
                        let index = displayRow * 4 + displayCol
                        nextBlockContainer.subviews[index].backgroundColor = color
                    }
                }
            }
        }
    }

    @objc private func backTapped() {
        SoundManager.shared.playValidClickSound()
        gameEngine.stopGame()
        MusicService.shared.startMusic()

        if GameSettings.shared.animationEnabled {
            UIView.animate(withDuration: 1.0, animations: {
                self.view.alpha = 0
            }, completion: { _ in
                self.dismiss(animated: false)
            })
        } else {
            dismiss(animated: true)
        }
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

            if GameSettings.shared.animationEnabled {
                UIView.animate(withDuration: 1.0, animations: {
                    self?.view.alpha = 0
                }, completion: { _ in
                    self?.dismiss(animated: false)
                })
            } else {
                self?.dismiss(animated: true)
            }
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

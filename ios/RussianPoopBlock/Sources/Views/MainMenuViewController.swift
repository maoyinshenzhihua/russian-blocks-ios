import UIKit

class MainMenuViewController: UIViewController {

    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "俄罗斯粑粑块"
        label.font = UIFont.boldSystemFont(ofSize: 48)
        label.textColor = .black
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    private let stackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 20
        stack.alignment = .fill
        stack.distribution = .fillEqually
        stack.translatesAutoresizingMaskIntoConstraints = false
        return stack
    }()

    private lazy var startGameBtn = createMenuButton(title: "开始游戏")
    private lazy var gameRecordBtn = createMenuButton(title: "游戏记录")
    private lazy var gameSettingsBtn = createMenuButton(title: "游戏设置")
    private lazy var aboutBtn = createMenuButton(title: "应用关于")

    private let copyrightLabel: UILabel = {
        let label = UILabel()
        label.text = "© 2026 俄罗斯粑粑块"
        label.font = UIFont.systemFont(ofSize: 14)
        label.textColor = .black
        label.textAlignment = .center
        label.alpha = 0.7
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupActions()

        SoundManager.shared.initSoundManager(soundEnabled: GameSettings.shared.gameSoundEnabled)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.setNavigationBarHidden(true, animated: animated)

        if GameSettings.shared.musicEnabled {
            MusicService.shared.startMusic()
        }
    }

    private func setupUI() {
        view.backgroundColor = UIColor(hex: "#FFD959")

        view.addSubview(titleLabel)
        view.addSubview(stackView)
        view.addSubview(copyrightLabel)

        stackView.addArrangedSubview(startGameBtn)
        stackView.addArrangedSubview(gameRecordBtn)
        stackView.addArrangedSubview(gameSettingsBtn)
        stackView.addArrangedSubview(aboutBtn)

        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 60),
            titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 40),
            titleLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -40),

            stackView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 60),
            stackView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 40),
            stackView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -40),

            copyrightLabel.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -30),
            copyrightLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor)
        ])
    }

    private func createMenuButton(title: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = UIFont.boldSystemFont(ofSize: 22)
        button.setTitleColor(.black, for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 8
        button.layer.borderWidth = 2
        button.layer.borderColor = UIColor.black.cgColor
        button.heightAnchor.constraint(equalToConstant: 65).isActive = true
        return button
    }

    private func setupActions() {
        startGameBtn.addTarget(self, action: #selector(startGameTapped), for: .touchUpInside)
        gameRecordBtn.addTarget(self, action: #selector(gameRecordTapped), for: .touchUpInside)
        gameSettingsBtn.addTarget(self, action: #selector(gameSettingsTapped), for: .touchUpInside)
        aboutBtn.addTarget(self, action: #selector(aboutTapped), for: .touchUpInside)
    }

    @objc private func startGameTapped() {
        SoundManager.shared.playValidClickSound()
        MusicService.shared.stopMusic()
        let gameVC = GameViewController()
        gameVC.modalPresentationStyle = .fullScreen
        present(gameVC, animated: true)
    }

    @objc private func gameRecordTapped() {
        SoundManager.shared.playValidClickSound()
        let recordVC = GameRecordViewController()
        navigationController?.pushViewController(recordVC, animated: true)
    }

    @objc private func gameSettingsTapped() {
        SoundManager.shared.playValidClickSound()
        let settingsVC = SettingsViewController()
        navigationController?.pushViewController(settingsVC, animated: true)
    }

    @objc private func aboutTapped() {
        SoundManager.shared.playValidClickSound()
        let aboutVC = AboutViewController()
        navigationController?.pushViewController(aboutVC, animated: true)
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        addButtonTargets()
    }

    private func addButtonTargets() {
        for case let button as UIButton in stackView.arrangedSubviews {
            button.removeTarget(self, action: #selector(buttonTouchDown(_:)), for: .allEvents)
            button.removeTarget(self, action: #selector(buttonTouchUp(_:)), for: .allEvents)
            button.addTarget(self, action: #selector(buttonTouchDown(_:)), for: .touchDown)
            button.addTarget(self, action: #selector(buttonTouchUp(_:)), for: [.touchUpInside, .touchUpOutside, .touchCancel])
        }
    }

    @objc private func buttonTouchDown(_ sender: UIButton) {
        UIView.animate(withDuration: 0.1) {
            sender.layer.borderColor = UIColor.systemBlue.cgColor
            sender.layer.borderWidth = 3
        }
    }

    @objc private func buttonTouchUp(_ sender: UIButton) {
        UIView.animate(withDuration: 0.1) {
            sender.layer.borderColor = UIColor.black.cgColor
            sender.layer.borderWidth = 2
        }
    }
}

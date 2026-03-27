import AVFoundation
import UIKit

class SoundManager {
    static let shared = SoundManager()

    private var soundPool: AVAudioEngine?
    private var playerNodes: [String: AVAudioPlayerNode] = [:]
    private var audioBuffers: [String: AVAudioPCMBuffer] = [:]
    private var isSoundEnabled: Bool = true

    enum SoundType: String {
        case validClick = "validclick"
        case invalidOperation = "invalidoperation"
        case jidan = "jidan"
        case baicai = "baicai"
        case yantou = "yantou"
        case baba = "baba"
        case shitou = "shitou"
        case jieshu = "jieshu"
    }

    private init() {
        setupAudioSession()
    }

    private func setupAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: [.mixWithOthers])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }

    func initSoundManager(soundEnabled: Bool) {
        isSoundEnabled = soundEnabled

        soundPool = AVAudioEngine()

        guard let engine = soundPool else { return }

        do {
            try engine.start()
        } catch {
            print("Failed to start audio engine: \(error)")
        }
    }

    func preloadSound(_ soundType: SoundType) {
        guard let url = Bundle.main.url(forResource: soundType.rawValue, withExtension: "mp3"),
              let file = try? AVAudioFile(forReading: url) else {
            return
        }

        let frameCount = AVAudioFrameCount(file.length)
        guard let buffer = try? AVAudioPCMBuffer(pcmFormat: file.processingFormat, frameCapacity: frameCount) else {
            return
        }

        do {
            try file.read(into: buffer)
        } catch {
            print("Failed to read audio file: \(error)")
            return
        }

        audioBuffers[soundType.rawValue] = buffer

        let playerNode = AVAudioPlayerNode()
        soundPool?.attach(playerNode)
        soundPool?.connect(playerNode, to: soundPool!.mainMixerNode, format: buffer.format)
        playerNodes[soundType.rawValue] = playerNode
    }

    func playSound(_ soundType: SoundType) {
        guard isSoundEnabled else { return }
        guard let buffer = audioBuffers[soundType.rawValue],
              let playerNode = playerNodes[soundType.rawValue] else {
            preloadAndPlay(soundType)
            return
        }

        if playerNode.isPlaying {
            playerNode.stop()
        }

        playerNode.scheduleBuffer(buffer, at: nil, options: [], completionHandler: nil)
        playerNode.play()
    }

    private func preloadAndPlay(_ soundType: SoundType) {
        guard let url = Bundle.main.url(forResource: soundType.rawValue, withExtension: "mp3"),
              let file = try? AVAudioFile(forReading: url) else {
            return
        }

        let format = file.processingFormat
        let frameCount = AVAudioFrameCount(file.length)
        guard let buffer = try? AVAudioPCMBuffer(pcmFormat: format, frameCapacity: frameCount) else {
            return
        }

        do {
            try file.read(into: buffer)
        } catch {
            return
        }

        audioBuffers[soundType.rawValue] = buffer

        let playerNode = AVAudioPlayerNode()
        soundPool?.attach(playerNode)
        soundPool?.connect(playerNode, to: soundPool!.mainMixerNode, format: format)
        playerNodes[soundType.rawValue] = playerNode

        playerNode.scheduleBuffer(buffer, at: nil, options: [], completionHandler: nil)
        playerNode.play()
    }

    func playValidClickSound() {
        playSound(.validClick)
    }

    func playInvalidOperationSound() {
        playSound(.invalidOperation)
    }

    func setSoundEnabled(_ enabled: Bool) {
        isSoundEnabled = enabled
    }

    func release() {
        soundPool?.stop()
        soundPool = nil
        playerNodes.removeAll()
        audioBuffers.removeAll()
    }
}

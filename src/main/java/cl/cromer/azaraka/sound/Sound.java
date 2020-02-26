/*
 * Copyright 2020 Chris Cromer
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package cl.cromer.azaraka.sound;

import cl.cromer.azaraka.Constants;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * This class handles sound
 */
public class Sound implements Constants {
	/**
	 * The path to the sound
	 */
	private final String path;
	/**
	 * The logger
	 */
	private final Logger logger;
	/**
	 * The sound clip to play
	 */
	private Clip sound;

	/**
	 * Load the sound
	 *
	 * @param path The path to the sound resource
	 * @throws SoundException Thrown if the sound file could not be loaded
	 */
	public Sound(String path) throws SoundException {
		this.path = path;
		logger = getLogger(this.getClass(), LogLevel.SOUND);
		InputStream inputStream = this.getClass().getResourceAsStream(path);
		if (inputStream == null) {
			throw new SoundException("Could not load sound: " + path);
		}
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
			DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
			sound = (Clip) AudioSystem.getLine(info);
			sound.open(audioInputStream);
			audioInputStream.close();
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			logger.warning(e.getMessage());
		}
		finally {
			if (sound != null && isPlaying()) {
				sound.stop();
			}
		}
		logger.info("Opened sound: " + path);
	}

	/**
	 * Play the sound
	 *
	 * @throws SoundException Thrown if the sound clip is null
	 */
	public void play() throws SoundException {
		if (sound == null) {
			throw new SoundException("Sound is null!");
		}

		// Stop the sound if it was already playing
		if (isPlaying()) {
			sound.stop();
		}

		sound.setFramePosition(0);
		sound.start();
		logger.info("Play sound: " + path);
	}

	/**
	 * Check if the sound clip is playing or not
	 *
	 * @return Returns true if the sound is playing or false otherwise
	 * @throws SoundException Thrown if the sound clip is null
	 */
	public boolean isPlaying() throws SoundException {
		if (sound == null) {
			throw new SoundException("Sound is null!");
		}

		return sound.isActive();
	}

	/**
	 * Stop the sound
	 *
	 * @throws SoundException Thrown if the sound clip is null
	 */
	public void stop() throws SoundException {
		if (sound == null) {
			throw new SoundException("Sound is null!");
		}

		if (isPlaying()) {
			sound.stop();
		}
		logger.info("Stop sound: " + path);
	}

	/**
	 * Set the number of loops to play
	 *
	 * @param loops The number of loops, should be n-1
	 * @throws SoundException Thrown if the sound is null
	 */
	public void setLoops(int loops) throws SoundException {
		if (sound == null) {
			throw new SoundException("Sound is null!");
		}
		sound.loop(loops);
	}

	/**
	 * Set the volume of the sound
	 *
	 * @param volume Volume between 0f and 1f
	 * @throws SoundException Thrown if the sound clip is null or the volume is out of range
	 */
	public void setVolume(float volume) throws SoundException {
		if (sound == null) {
			throw new SoundException("Sound is null!");
		}

		if (volume < 0f || volume > 1f) {
			throw new SoundException("Invalid sound range!");
		}

		if (sound.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			FloatControl gainControl = (FloatControl) sound.getControl(FloatControl.Type.MASTER_GAIN);
			float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
			logger.info("Set volume " + path + ": " + dB);
			gainControl.setValue(dB);
		}
		else if (sound.isControlSupported(FloatControl.Type.VOLUME)) {
			logger.info("Gain control not supported");
			float newVolume = 0;
			if (volume > 0f) {
				newVolume = ((volume * 100) * 65536) / 100;
			}
			FloatControl volumeControl = (FloatControl) sound.getControl(FloatControl.Type.VOLUME);
			logger.info("Set volume " + path + ": " + newVolume);
			volumeControl.setValue(newVolume);
		}
		else {
			logger.info("No control to modify volume");
		}
	}
}

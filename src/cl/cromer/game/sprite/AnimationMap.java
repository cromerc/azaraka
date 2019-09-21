/*
 * Copyright 2019 Chris Cromer
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

package cl.cromer.game.sprite;

import cl.cromer.game.Constantes;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class is used to copy the sprite into a new sprite object so that the sprite doesn't get passed by reference
 * This is important because 2 cells share the same sprite, but not the same frame of animation
 */
public class AnimationMap extends HashMap<Constantes.SpriteType, Animation> implements Constantes {
	/**
	 * Clone the sprite object when returning
	 *
	 * @param key The key used to get the object
	 * @return Return the clone of the sprite
	 */
	@Override
	public Animation get(Object key) {
		try {
			return (Animation) super.get(key).clone();
		}
		catch (CloneNotSupportedException e) {
			Logger logger = getLogger(this.getClass(), IMAGE_LOG_LEVEL);
			logger.warning(e.getMessage());
		}
		return null;
	}
}

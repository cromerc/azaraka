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

package cl.cromer.azaraka.json;

import cl.cromer.azaraka.Cell;
import cl.cromer.azaraka.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * This class handles reading and writing of JSON objects
 */
public class Json implements Constants {
	/**
	 * The logger
	 */
	private final Logger logger;

	/**
	 * Initialize the JSON object
	 */
	public Json() {
		logger = getLogger(this.getClass(), LogLevel.JSON);
	}

	/**
	 * Export the game cells to a JSON ready object then write it to a file
	 *
	 * @param cells The cells of the scene to export
	 */
	public void exportScene(CopyOnWriteArrayList<CopyOnWriteArrayList<Cell>> cells) {
		JsonCell[][] jsonCells = new JsonCell[cells.size()][cells.get(0).size()];
		for (int x = 0; x < cells.size(); x++) {
			for (int y = 0; y < cells.get(x).size(); y++) {
				jsonCells[x][y] = new JsonCell();
				if (cells.get(x).get(y).getObject() != null) {
					jsonCells[x][y].type = cells.get(x).get(y).getObject().getClass().getName();
				}
				else {
					jsonCells[x][y].type = "null";
				}
				jsonCells[x][y].textures = cells.get(x).get(y).getTextureNumbers();
			}
		}
		writeScene(jsonCells);
	}

	/**
	 * Write the JSON scene to a file
	 *
	 * @param jsonCells The JSON cells object
	 */
	private void writeScene(JsonCell[][] jsonCells) {
		GsonBuilder gsonBuilder;
		if (PRETTY_JSON) {
			gsonBuilder = new GsonBuilder().setPrettyPrinting();
		}
		else {
			gsonBuilder = new GsonBuilder();
		}
		Gson gson = gsonBuilder.create();
		String json = gson.toJson(jsonCells);

		File file = new File("src/main/resources/scene.json");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(json.getBytes());
			fileOutputStream.close();
		}
		catch (IOException e) {
			logger.warning(e.getMessage());
		}
	}
}

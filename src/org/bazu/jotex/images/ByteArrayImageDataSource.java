/*
 * ByteArrayImageDataSource.java
 * 
 * Copyright (c) 2011, Luca Conte. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.bazu.jotex.images;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.adobe.dp.epub.io.DataSource;

public class ByteArrayImageDataSource extends DataSource {
	private byte[] content;
	public ByteArrayImageDataSource() {
		super();
	}
	public ByteArrayImageDataSource(byte[] content) {
		super();
		this.content = content;
	}
	@Override
	public InputStream getInputStream() throws IOException {
		
		return new ByteArrayInputStream(getContent());
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}

}

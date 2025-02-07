/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 *******************************************************************************/
package com.ibm.biginsights.project.util;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


public class ImageDecorator extends CompositeImageDescriptor{
	
	Image baseImage=null;
	Image decoratorImage=null;
	int x_decoroator = 0;
	int y_decoroator = 0;
	
	
	public ImageDecorator(Image baseImage, Image decoratorImage, int x_decoroator, int y_decoroator) {
		super();
		this.baseImage = baseImage;
		this.decoratorImage = decoratorImage;
		this.x_decoroator = x_decoroator;
		this.y_decoroator = y_decoroator;
	}

	@Override
	protected void drawCompositeImage(int width, int height) {
		drawImage(baseImage.getImageData(), 0, 0);
		drawImage(decoratorImage.getImageData(), x_decoroator, y_decoroator);
	}

	@Override
	protected Point getSize() {
		if(baseImage != null){
			Rectangle rec = baseImage.getBounds();
			return new Point(rec.width, rec.height);
		}
		return null;
	}

	
}

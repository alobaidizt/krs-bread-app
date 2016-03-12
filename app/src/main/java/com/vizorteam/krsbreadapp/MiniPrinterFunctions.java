package com.vizorteam.krsbreadapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MiniPrinterFunctions {
	enum BarcodeWidth {
		_125, _250, _375, _500, _625, _750, _875, _1_0
	};

	enum BarcodeType {
		code39, ITF, code93, code128
	};

	public enum Alignment {
		Left, Center, Right
	};

	public enum CorrectionLevelOption {
		Low, Middle, Q, High
	};

	private static StarIOPort portForMoreThanOneFunction = null;

	public static void AddRange(ArrayList<byte[]> array, byte[] newData) {
		for (int index = 0; index < newData.length; index++) {
			array.add(newData);
		}
	}

	/**
	 * This function is not supported by portable(ESC/POS) printers.
	 * 
	 * @param context
	 *     Activity for displaying messages
	 * @param portName
	 *     Port name to use for communication
	 * @param portSettings
	 *     The port settings to use
	 */
	public static void OpenCashDrawer(Context context, String portName, String portSettings) {
		Builder dialog = new AlertDialog.Builder(context);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle("Feature Not Available");
		alert.setMessage("Cash drawer functionality is supported only on POS printer models");
		alert.setCancelable(false);
		alert.show();
	}

	/**
	 * This function shows how to get the firmware information of a printer
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 */
	public static void CheckFirmwareVersion(Context context, String portName, String portSettings) {
		StarIOPort port = null;
		try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

			// A sleep is used to get time for the socket to completely open
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			Map<String, String> firmware = port.getFirmwareInformation();

			String modelName = firmware.get("ModelName");
			String firmwareVersion = firmware.get("FirmwareVersion");

			String message = "Model Name:" + modelName;
			message += "\nFirmware Version:" + firmwareVersion;

			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Firmware Information");
			alert.setMessage(message);
			alert.setCancelable(false);
			alert.show();

		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage("Failed to connect to printer");
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}
	}

	/**
	 * This function shows how to get the status of a printer
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 */
	public static void CheckStatus(Context context, String portName, String portSettings) {
		StarIOPort port = null;
		try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
			port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */

			// A sleep is used to get time for the socket to completely open
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			StarPrinterStatus status = port.retreiveStatus();

			if (status.offline == false) {
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				alert.setTitle("Printer");
				alert.setMessage("Printer is Online");
				alert.setCancelable(false);
				alert.show();
			} else {
				String message = "Printer is offline";
				if (status.receiptPaperEmpty == true) {
					message += "\nPaper is Empty";
				}
				if (status.coverOpen == true) {
					message += "\nCover is Open";
				}
				Builder dialog = new AlertDialog.Builder(context);
				dialog.setNegativeButton("Ok", null);
				AlertDialog alert = dialog.create();
				alert.setTitle("Printer");
				alert.setMessage(message);
				alert.setCancelable(false);
				alert.show();
			}
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage("Failed to connect to printer");
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}
	}

	/**
	 * This function is used to print any of the barcodes supported by portable(ESC/POS) printers This example supports 4 barcode types code39, code93, ITF, code128. For a complete list of supported barcodes see manual (pg 35).
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param height
	 *     The height of the barcode, max is 255
	 * @param width
	 *     Sets the width of the barcode, value of this should be 1 to 8. See pg 34 of the manual for the definitions of the values.
	 * @param type
	 *     The type of barcode to print. This program supports code39, code93, ITF, code128.
	 * @param barcodeData
	 *     The data to print. The type of characters supported varies. See pg 35 for a complete list of all support characters
	 */
	public static void PrintBarcode(Context context, String portName, String portSettings, byte height, BarcodeWidth width, BarcodeType type, byte[] barcodeData) {
		ArrayList<byte[]> commands = new ArrayList<byte[]>();

		byte[] height_Commands = new byte[] { 0x1d, 0x68, 0x00 };
		height_Commands[2] = height;
		commands.add(height_Commands);

		byte[] width_Commands = new byte[] { 0x1d, 0x77, 0x00 };
		switch (width) {
		case _125:
			width_Commands[2] = 1;
			break;
		case _250:
			width_Commands[2] = 2;
			break;
		case _375:
			width_Commands[2] = 3;
			break;
		case _500:
			width_Commands[2] = 4;
			break;
		case _625:
			width_Commands[2] = 5;
			break;
		case _750:
			width_Commands[2] = 6;
			break;
		case _875:
			width_Commands[2] = 7;
			break;
		case _1_0:
			width_Commands[2] = 8;
			break;
		}
		commands.add(width_Commands);

		byte[] print_Barcode = new byte[4 + barcodeData.length + 1];
		print_Barcode[0] = 0x1d;
		print_Barcode[1] = 0x6b;
		switch (type) {
		case code39:
			print_Barcode[2] = 69;
			break;
		case ITF:
			print_Barcode[2] = 70;
			break;
		case code93:
			print_Barcode[2] = 72;
			break;
		case code128:
			print_Barcode[2] = 73;
			break;
		}
		print_Barcode[3] = (byte) barcodeData.length;
		System.arraycopy(barcodeData, 0, print_Barcode, 4, barcodeData.length);

		commands.add(print_Barcode);

		commands.add(new byte[]{0x0a, 0x0a, 0x0a, 0x0a});

		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * The function is used to print a QRCode for portable(ESC/POS) printers
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for bluetooth)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param correctionLevel
	 *     The correction level for the QRCode. This value should be 0x4C, 0x4D, 0x51, or 0x48. See pg 41 for for definition of values
	 * @param sizeByECLevel
	 *     This specifies the symbol version. This value should be 1 to 40. See pg 41 for the definition of the level
	 * @param moduleSize
	 *     The module size of the QRCode. This value should be 1 to 8.
	 * @param barcodeData
	 *     The characters to print in the QRCode
	 */
	public static void PrintQrcode(Context context, String portName, String portSettings, CorrectionLevelOption correctionLevel, byte sizeByECLevel, byte moduleSize, byte[] barcodeData) {
		ArrayList<byte[]> commands = new ArrayList<byte[]>();

		// The printer supports 3 2d bar code types, this one selects qrcode
		commands.add(new byte[] { 0x1d, 0x5a, 0x02 });

		// This builds the qrcommand
		byte[] print2dbarcode = new byte[7 + barcodeData.length];
		print2dbarcode[0] = 0x1b;
		print2dbarcode[1] = 0x5a;
		print2dbarcode[2] = sizeByECLevel;
		switch (correctionLevel) {
		case Low:
			print2dbarcode[3] = 'L';
			break;
		case Middle:
			print2dbarcode[3] = 'M';
			break;
		case Q:
			print2dbarcode[3] = 'Q';
			break;
		case High:
			print2dbarcode[3] = 'H';
			break;
		}
		print2dbarcode[4] = moduleSize;
		print2dbarcode[5] = (byte) (barcodeData.length % 256);
		print2dbarcode[6] = (byte) (barcodeData.length / 256);
		System.arraycopy(barcodeData, 0, print2dbarcode, 7, barcodeData.length);
		commands.add(print2dbarcode);

		commands.add(new byte[]{0x0a, 0x0a, 0x0a, 0x0a});

		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * This function prints PDF417 barcodes for portable(ESC/POS) printers
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<DeviceName> for Bluetooth)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param width
	 *     This is the width of the PDF417 barcode to print. This is the same width used by the 1D barcodes. See pg 34 of the command manual.
	 * @param columnNumber
	 *     This is the column number of the PDF417 barcode to print. The value of this should be between 1 and 30.
	 * @param securityLevel
	 *     The represents how well the barcode can be restored if damaged. The value should be between 0 and 8.
	 * @param ratio
	 *     The value representing the horizontal and vertical ratio of the barcode. This value should between 2 and 5.
	 * @param barcodeData
	 *     The characters that will be in the barcode
	 */
	public static void PrintPDF417(Context context, String portName, String portSettings, BarcodeWidth width, byte columnNumber, byte securityLevel, byte ratio, byte[] barcodeData) {
		ArrayList<byte[]> commands = new ArrayList<byte[]>();

		byte[] barcodeWidthCommand = new byte[] { 0x1d, 'w', 0x00 };
		switch (width) {
		case _125:
			barcodeWidthCommand[2] = 1;
			break;
		case _250:
			barcodeWidthCommand[2] = 2;
			break;
		case _375:
			barcodeWidthCommand[2] = 3;
			break;
		case _500:
			barcodeWidthCommand[2] = 4;
			break;
		case _625:
			barcodeWidthCommand[2] = 5;
			break;
		case _750:
			barcodeWidthCommand[2] = 6;
			break;
		case _875:
			barcodeWidthCommand[2] = 7;
			break;
		case _1_0:
			barcodeWidthCommand[2] = 8;
			break;
		}

		commands.add(barcodeWidthCommand);

		commands.add(new byte[] { 0x1d, 0x5a, 0x00 });

		byte[] barcodeCommand = new byte[7 + barcodeData.length];
		barcodeCommand[0] = 0x1b;
		barcodeCommand[1] = 0x5a;
		barcodeCommand[2] = columnNumber;
		barcodeCommand[3] = securityLevel;
		barcodeCommand[4] = ratio;
		barcodeCommand[5] = (byte) (barcodeData.length % 256);
		barcodeCommand[6] = (byte) (barcodeData.length / 256);

		System.arraycopy(barcodeData, 0, barcodeCommand, 7, barcodeData.length);
		commands.add(barcodeCommand);

		commands.add(new byte[]{0x0a, 0x0a, 0x0a, 0x0a});

		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * Cut is not supported on portable(ESC/POS) printers
	 *
	 * @param context
	 *     Activity to send the message that cut is not supported to the user
	 */
	public static void performCut(Context context) {
		Builder dialog = new AlertDialog.Builder(context);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle("Feature Not Available");
		alert.setMessage("Cut functionality is supported only on POS printer models");
		alert.setCancelable(false);
		alert.show();
	}

	/**
	 * This function is used to print a java bitmap directly to a portable(ESC/POS) printer.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param source
	 *     The bitmap to convert to Star printer data for portable(ESC/POS) printers
	 * @param maxWidth
	 *     The maximum width of the image to print. This is usually the page width of the printer. If the image exceeds the maximum width then the image is scaled down. The ratio is maintained.
	 */

	/**
	 * This function prints raw text to a Star portable(ESC/POS) printer. It shows how the text can be modified like changing its size.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param underline
	 *     boolean variable that tells the printer to underline the text
	 * @param emphasized
	 *     boolean variable that tells the printer to emphasize the text. This is somewhat like bold. It isn't as dark, but darker than regular characters.
	 * @param upsideDown
	 *     boolean variable that tells the printer to print text upside down.
	 * @param invertColor
	 *     boolean variable that tells the printer to invert text. All white space will become black but the characters will be left white.
	 * @param heightExpansion
	 *     This integer tells the printer what the character height should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param widthExpansion
	 *     This integer tell the printer what the character width should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param leftMargin
	 *     Defines the left margin for text on Star portable(ESC/POS) printers. This number can be from 0 to 65536. However, remember how much space is available as the text can be pushed off the page.
	 * @param alignment
	 *     Defines the alignment of the text. The printers support left, right, and center justification.
	 * @param textToPrint
	 *     The text to send to the printer.
	 */
	public static void PrintText(Context context, String portName, String portSettings, boolean underline, boolean emphasized, boolean upsideDown, boolean invertColor, byte heightExpansion, byte widthExpansion, int leftMargin, Alignment alignment, byte[] textToPrint) {
		ArrayList<byte[]> commands = new ArrayList<byte[]>();

		commands.add(new byte[] { 0x1b, 0x40 }); // Initialization

		byte[] underlineCommand = new byte[] { 0x1b, 0x2d, 0x00 };
		if (underline) {
			underlineCommand[2] = 49;
		} else {
			underlineCommand[2] = 48;
		}
		commands.add(underlineCommand);

		byte[] emphasizedCommand = new byte[] { 0x1b, 0x45, 0x00 };
		if (emphasized) {
			emphasizedCommand[2] = 1;
		} else {
			emphasizedCommand[2] = 0;
		}
		commands.add(emphasizedCommand);

		byte[] upsidedownCommand = new byte[] { 0x1b, 0x7b, 0x00 };
		if (upsideDown) {
			upsidedownCommand[2] = 1;
		} else {
			upsidedownCommand[2] = 0;
		}
		commands.add(upsidedownCommand);

		byte[] invertColorCommand = new byte[] { 0x1d, 0x42, 0x00 };
		if (invertColor) {
			invertColorCommand[2] = 1;
		} else {
			invertColorCommand[2] = 0;
		}
		commands.add(invertColorCommand);

		byte[] characterSizeCommand = new byte[] { 0x1d, 0x21, 0x00 };
		characterSizeCommand[2] = (byte) (heightExpansion | (widthExpansion << 4));
		commands.add(characterSizeCommand);

		byte[] leftMarginCommand = new byte[] { 0x1d, 0x4c, 0x00, 0x00 };
		leftMarginCommand[2] = (byte) (leftMargin % 256);
		leftMarginCommand[3] = (byte) (leftMargin / 256);
		commands.add(leftMarginCommand);

		byte[] justificationCommand = new byte[] { 0x1b, 0x61, 0x00 };
		switch (alignment) {
		case Left:
			justificationCommand[2] = 48;
			break;
		case Center:
			justificationCommand[2] = 49;
			break;
		case Right:
			justificationCommand[2] = 50;
			break;
		}
		commands.add(justificationCommand);

		commands.add(textToPrint);

		commands.add(new byte[] {0x0a});

		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * This function prints raw JP-Kanji text to a Star portable(ESC/POS) printer. It shows how the text can be modified like changing its size.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param underline
	 *     boolean variable that tells the printer to underline the text
	 * @param emphasized
	 *     boolean variable that tells the printer to emphasize the text. This is somewhat like bold. It isn't as dark, but darker than regular characters.
	 * @param upsideDown
	 *     boolean variable that tells the printer to print text upside down.
	 * @param invertColor
	 *     boolean variable that tells the printer to invert text. All white space will become black but the characters will be left white.
	 * @param heightExpansion
	 *     This integer tells the printer what the character height should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param widthExpansion
	 *     This integer tell the printer what the character width should be, ranging from 0 to 7 and representing multiples from 1 to 8.
	 * @param leftMargin
	 *     Defines the left margin for text on Star portable(ESC/POS) printers. This number can be from 0 to 65536. However, remember how much space is available as the text can be pushed off the page.
	 * @param alignment
	 *     Defines the alignment of the text. The printers support left, right, and center justification.
	 * @param textToPrint
	 *     The text to send to the printer.
	 */
	public static void PrintTextKanji(Context context, String portName, String portSettings, boolean underline, boolean emphasized, boolean upsideDown, boolean invertColor, byte heightExpansion, byte widthExpansion, int leftMargin, Alignment alignment, byte[] textToPrint) {
		ArrayList<byte[]> commands = new ArrayList<byte[]>();

		commands.add(new byte[] { 0x1b, 0x40 }); // Initialization

		commands.add(new byte[] { 0x1c, 0x43, 0x31 }); // Shift-JIS Kanji Mode

		byte[] underlineCommand = new byte[] { 0x1b, 0x2d, 0x00 };
		if (underline) {
			underlineCommand[2] = 49;
		} else {
			underlineCommand[2] = 48;
		}
		commands.add(underlineCommand);

		byte[] emphasizedCommand = new byte[] { 0x1b, 0x45, 0x00 };
		if (emphasized) {
			emphasizedCommand[2] = 1;
		} else {
			emphasizedCommand[2] = 0;
		}
		commands.add(emphasizedCommand);

		byte[] upsidedownCommand = new byte[] { 0x1b, 0x7b, 0x00 };
		if (upsideDown) {
			upsidedownCommand[2] = 1;
		} else {
			upsidedownCommand[2] = 0;
		}
		commands.add(upsidedownCommand);

		byte[] invertColorCommand = new byte[] { 0x1d, 0x42, 0x00 };
		if (invertColor) {
			invertColorCommand[2] = 1;
		} else {
			invertColorCommand[2] = 0;
		}
		commands.add(invertColorCommand);

		byte[] characterSizeCommand = new byte[] { 0x1d, 0x21, 0x00 };
		characterSizeCommand[2] = (byte) (heightExpansion | (widthExpansion << 4));
		commands.add(characterSizeCommand);

		byte[] leftMarginCommand = new byte[] { 0x1d, 0x4c, 0x00, 0x00 };
		leftMarginCommand[2] = (byte) (leftMargin % 256);
		leftMarginCommand[3] = (byte) (leftMargin / 256);
		commands.add(leftMarginCommand);

		byte[] justificationCommand = new byte[] { 0x1b, 0x61, 0x00 };
		switch (alignment) {
		case Left:
			justificationCommand[2] = 48;
			break;
		case Center:
			justificationCommand[2] = 49;
			break;
		case Right:
			justificationCommand[2] = 50;
			break;
		}
		commands.add(justificationCommand);

		// textToPrint Encoding!!
		String strData = new String(textToPrint);
		byte[] rawData = null;
		try {
			rawData = strData.getBytes("Shift_JIS"); // Shift JIS code
		} catch (UnsupportedEncodingException e) {
			rawData = strData.getBytes();
		}

		commands.add(rawData);

		commands.add(new byte[] {0x0a});

		sendCommand(context, portName, portSettings, commands);
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 */
	public static void MCRStart(final Context context, String portName, String portSettings) {
		try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
			portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 portForMoreThanOneFunction = StarIOPort.getPort(portName, portSettings, 10000);
			 */

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			portForMoreThanOneFunction.writePort(new byte[] { 0x1b, 0x4d, 0x45 }, 0, 3);

			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Cancel", new OnClickListener() {
				// If the user cancels MSR mode, the character 0x04 is sent to the printer
				// This function also closes the port
				public void onClick(DialogInterface dialog, int which) {
					((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
					((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
					try {
						portForMoreThanOneFunction.writePort(new byte[] { 0x04 }, 0, 1);
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
						}
					} catch (StarIOPortException e) {

					} finally {
						if (portForMoreThanOneFunction != null) {
							try {
								StarIOPort.releasePort(portForMoreThanOneFunction);
							} catch (StarIOPortException e1) {
							}
						}
					}
				}
			});
			AlertDialog alert = dialog.create();
			alert.setTitle("");
			alert.setMessage("Slide credit card");
			alert.setCancelable(false);
			alert.setButton("OK", new OnClickListener() {
				// If the user presses ok then the magnetic stripe is read and displayed to the user
				// This function also closes the port
				public void onClick(DialogInterface dialog, int which) {
					((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
					((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
					try {
						byte[] mcrData = new byte[100];
						portForMoreThanOneFunction.readPort(mcrData, 0, mcrData.length);

						Builder dialog1 = new AlertDialog.Builder(context);
						dialog1.setNegativeButton("Ok", null);
						AlertDialog alert = dialog1.create();
						alert.setTitle("");
						alert.setMessage(new String(mcrData));
						alert.show();

						portForMoreThanOneFunction.writePort(new byte[] { 0x04 }, 0, 1);
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
						}

					} catch (StarIOPortException e) {

					} finally {
						if (portForMoreThanOneFunction != null) {
							try {
								StarIOPort.releasePort(portForMoreThanOneFunction);
							} catch (StarIOPortException e1) {
							}
						}
					}
				}
			});
			alert.show();
		} catch (StarIOPortException e) {
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage("Failed to connect to printer");
			alert.setCancelable(false);
			alert.show();
			if (portForMoreThanOneFunction != null) {
				try {
					StarIOPort.releasePort(portForMoreThanOneFunction);
				} catch (StarIOPortException e1) {
				}
			}
		} finally {

		}
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param strPrintArea
	 *     Printable area size, This should be ("2inch (58mm)" or "3inch (80mm)")
	 */
	public static boolean PrintSampleReceipt(Context context, String portName, String portSettings, String strPrintArea) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();

		if (strPrintArea.equals("2inch (58mm)")) {
			byte[] outputByteBuffer = null;
			list.add(new byte[] { 0x1d, 0x57, (byte) 0x80, 0x31 }); // Page Area Setting <GS> <W> nL nH (nL = 128, nH = 1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)

			// outputByteBuffer = ("[Print Stored Logo Below]\n\n").getBytes();
			// port.writePort(outputByteBuffer, 0, outputByteBuffer.length);
			//
			// list.add(new byte[]{0x1b, 0x66, 0x00}); //Stored Logo Printing <ESC> f n (n = Store Logo # = 0 or 1 or 2 etc.)

			list.add(("Star Clothing Boutique\n" + "123 Star Road\n" + "City, State 12345\n\n").getBytes());

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

			list.add(("Date: MM/DD/YYYY   Time:HH:MM PM\n" + "--------------------------------\n").getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x01 }); // Set Emphasized Printing ON

			list.add("SALE\n".getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x00 }); // Set Emphasized Printing OFF (same command as on)

			outputByteBuffer = ("300678566  PLAIN T-SHIRT  10.99\n" + "300692003  BLACK DENIM    29.99\n" + "300651148  BLUE DENIM     29.99\n" + "300642980  STRIPED DRESS  49.99\n" + "300638471  BLACK BOOTS    35.99\n\n" + "Subtotal                 156.95" + "\n" + "Tax                        0.00" + "\n" + "--------------------------------\n" + "Total ").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x21, 0x11 }); // Width and Height Character Expansion <GS> ! n

			list.add("      $156.95\n".getBytes());

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Expansion - Reference Star Portable Printer Programming Manual

			list.add(("--------------------------------\n" + "Charge\n" + "$156.95\n" + "Visa XXXX-XXXX-XXXX-0123\n").getBytes());

			list.add(new byte[] { 0x1d, 0x77, 0x02 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x68, 0x64 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x48, 0x01 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x6b, 0x41, 0x0b, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30 }); // for 1D Code39 Barcode

			list.add("\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x01 }); // Specify White-Black Invert

			list.add("Refunds and Exchanges\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x00 }); // Cancel White-Black Invert

			list.add("Within ".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x01 }); // Specify Underline Printing

			list.add("30 days".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x00 }); // Cancel Underline Printing

			outputByteBuffer = (" with receipt\n"
								+ "And tags attached\n"
								+ "-------------Sign Here----------\n\n\n"
								+ "--------------------------------\n"
								+ "Thank you for buying Star!\n"
								+ "Scan QR code to visit our site!\n").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x5a, 0x02 }); // Cancel Underline Printing

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n".getBytes());

			return sendCommand(context, portName, portSettings, list);
		} else if (strPrintArea.equals("3inch (80mm)")) {
			byte[] outputByteBuffer = null;

			list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)

			// outputByteBuffer = ("[Print Stored Logo Below]\n\n").getBytes();
			// port.writePort(outputByteBuffer, 0, outputByteBuffer.length);
			//
			// list.add(new byte[]{0x1b, 0x66, 0x00}); //Stored Logo Printing <ESC> f n (n = Store Logo # = 0 or 1 or 2 etc.)

			list.add(("\nStar Clothing Boutique\n" + "123 Star Road\n" + "City, State 12345\n\n").getBytes());

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Setting Horizontal Tab

			list.add("Date: MM/DD/YYYY ".getBytes());

			list.add(new byte[] { 0x09 }); // Left Alignment"

			list.add(("Time: HH:MM PM\n" + "------------------------------------------------ \n").getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x01 }); // Set Emphasized Printing ON

			list.add("SALE\n".getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x00 }); // Set Emphasized Printing OFF (same command as on)

			outputByteBuffer = ("300678566    PLAIN T-SHIRT                 10.99\n"
								+ "300692003    BLACK DENIM                   29.99\n"
								+ "300651148    BLUE DENIM                    29.99\n"
								+ "300642980    STRIPED DRESS                 49.99\n"
								+ "300638471    BLACK BOOTS                   35.99\n\n"
								+ "Subtotal                                  156.95\n"
								+ "Tax                                         0.00\n"
								+ "------------------------------------------------ \n"
								+ "Total   ").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x21, 0x11 }); // Width and Height Character Expansion <GS> ! n

			list.add("             $156.95\n".getBytes());

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Expansion - Reference Star Portable Printer Programming Manual

			list.add(("------------------------------------------------ \n"
					+ "Charge\n"
					+ "$156.95\n"
					+ "Visa XXXX-XXXX-XXXX-0123\n").getBytes());

			list.add(new byte[] { 0x1d, 0x77, 0x02 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x68, 0x64 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x48, 0x01 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x6b, 0x41, 0x0b, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30 }); // for 1D Code39 Barcode

			list.add("\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x01 }); // Specify White-Black Invert

			list.add("Refunds and Exchanges\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x00 }); // Cancel White-Black Invert

			list.add("Within ".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x01 }); // Specify Underline Printing

			list.add("30 days".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x00 }); // Cancel Underline Printing

			outputByteBuffer = (" with receipt\n" + "And tags attached\n" + "------------- Card Holder's Signature ---------- \n\n\n" + "------------------------------------------------ \n" + "Thank you for buying Star!\n" + "Scan QR code to visit our site!\n").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x5a, 0x02 }); // Cancel Underline Printing

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());
		} else if (strPrintArea.equals("4inch (112mm)")) {
			byte[] outputByteBuffer = null;

			list.add(new byte[] { 0x1d, 0x57, 0x40, 0x32 }); // Page Area Setting <GS> <W> nL nH (nL = 64, nH = 2)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Center Justification <ESC> a n (0 Left, 1 Center, 2 Right)

			// outputByteBuffer = ("[Print Stored Logo Below]\n\n").getBytes();
			// port.writePort(outputByteBuffer, 0, outputByteBuffer.length);
			//
			// list.add(new byte[]{0x1b, 0x66, 0x00})); //Stored Logo Printing <ESC> f n (n = Store Logo # = 0 or 1 or 2 etc.)

			list.add(("\nStar Clothing Boutique\n" + "123 Star Road\n" + "City, State 12345\n\n").getBytes());

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Left Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x1b, 0x34, 0x00 }); // Setting Horizontal Tab

			list.add("Date: MM/DD/YYYY ".getBytes());

			list.add(new byte[] { 0x09 }); // Left Alignment"

			list.add(("Time: HH:MM PM\n" + "--------------------------------------------------------------------- \n").getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x01 }); // Set Emphasized Printing ON

			list.add("SALE\n".getBytes());

			list.add(new byte[] { 0x1b, 0x45, 0x00 }); // Set Emphasized Printing OFF (same command as on)

			outputByteBuffer = ("300678566 \u0009  PLAIN T-SHIRT\u0009         10.99\n"
								+ "300692003 \u0009  BLACK DENIM\u0009         29.99\n"
								+ "300651148 \u0009  BLUE DENIM\u0009         29.99\n"
								+ "300642980 \u0009  STRIPED DRESS\u0009         49.99\n"
								+ "300638471 \u0009  BLACK BOOTS\u0009         35.99\n\n"
								+ "Subtotal \u0009\u0009        156.95\n"
								+ "Tax \u0009\u0009          0.00\n"
								+ "--------------------------------------------------------------------- \n"
								+ "Total\u0009").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x21, 0x11 }); // Width and Height Character Expansion <GS> ! n

			list.add("\u0009$156.95\n".getBytes());

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Expansion - Reference Star Portable Printer Programming Manual

			outputByteBuffer = ("--------------------------------------------------------------------- \n"
								+ "Charge\n"
								+ "$156.95\n"
								+ "Visa XXXX-XXXX-XXXX-0123\n").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x77, 0x02 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x68, 0x64 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x48, 0x01 }); // for 1D Code39 Barcode
			list.add(new byte[] { 0x1d, 0x6b, 0x41, 0x0b, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30 }); // for 1D Code39 Barcode

			list.add("\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x01 }); // Specify White-Black Invert

			list.add("Refunds and Exchanges\n".getBytes());

			list.add(new byte[] { 0x1d, 0x42, 0x00 }); // Cancel White-Black Invert

			list.add("Within ".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x01 }); // Specify Underline Printing

			list.add("30 days".getBytes());

			list.add(new byte[] { 0x1b, 0x2d, 0x00 }); // Cancel Underline Printing

			outputByteBuffer = (" with receipt\n"
								+ "And tags attached\n"
								+ "----------------------- Card Holder's Signature --------------------- \n\n\n"
								+ "--------------------------------------------------------------------- \n"
								+ "Thank you for buying Star!\n"
								+ "Scan QR code to visit our site!\n").getBytes();
			list.add(outputByteBuffer);

			list.add(new byte[] { 0x1d, 0x5a, 0x02 }); // Cancel Underline Printing

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());
		}

		return sendCommand(context, portName, portSettings, list);
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param strPrintArea
	 *     Printable area size, This should be ("2inch (58mm)" or "3inch (80mm)")
	 */
	public static boolean PrintSampleReceipt_French(Context context, String portName, String portSettings, String strPrintArea) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();

		if (strPrintArea.equals("2inch (58mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x11 });

			list.add(createCp1252("\nORANGE\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Character Expansion

			list.add(createCp1252("36 AVENUE LA MOTTE PICQUET\r\n\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("Date   : MM/DD/YYYY\r\n"));
			list.add(createCp1252("Heure  : HH:MM\r\n"));

			list.add(createCp1252("Boutique: OLUA23    Caisse: 0001\r\n"));
			list.add(createCp1252("Conseiller: 002970  Ticket: 3881\r\n"));

			list.add(createCp1252("--------------------------------\r\n\r\n"));

			list.add(createCp1252("Vous avez été servi par : Souad\r\n\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE\r\n"));

			list.add(createCp1252("3700615033581 1 X 19.99€  19.99€\r\n\r\n"));

			list.add(createCp1252("dont contribution\r\n environnementale :\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE          0.01€\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("    1 Piéce(s)   Total :  19.99€\r\n"));

			list.add(createCp1252("      Mastercard Visa  :  19.99€\r\n\r\n"));

			list.add(createCp1252("Taux TVA    Montant H.T.   T.V.A\r\n"));

			list.add(createCp1252("  20%          16.66€      3.33€\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("Merci de votre visite et.\r\n"));

			list.add(createCp1252("à bientôt.\r\n"));

			list.add(createCp1252("Conservez votre ticket il\r\n"));

			list.add(createCp1252("vous sera demandé pour \r\n"));

			list.add(createCp1252("tout échange.\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 });

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("3inch (80mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x11 });

			list.add(createCp1252("\nORANGE\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Character Expansion

			list.add(createCp1252("36 AVENUE LA MOTTE PICQUET\r\n\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("Date   : MM/DD/YYYY   Heure: HH:MM\r\n"));

			list.add(createCp1252("Boutique: OLUA23      Caisse: 0001\r\n"));
			list.add(createCp1252("Conseiller: 002970    Ticket: 3881\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n\r\n"));

			list.add(createCp1252("Vous avez été servi par : Souad\r\n\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE\r\n"));

			list.add(createCp1252("3700615033581   1   X   19.99€       19.99€\r\n\r\n"));

			list.add(createCp1252("dont contribution environnementale :\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE                     0.01€\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("    1 Piéce(s)   Total :             19.99€\r\n"));

			list.add(createCp1252("      Mastercard Visa  :             19.99€\r\n\r\n"));

			list.add(createCp1252("Taux TVA    Montant H.T.   T.V.A\r\n"));

			list.add(createCp1252("  20%          16.66€      3.33€\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("Merci de votre visite et. à bientôt.\r\n"));

			list.add(createCp1252("Conservez votre ticket il\r\n"));

			list.add(createCp1252("vous sera demandé pour tout échange.\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 });

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("4inch (112mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x11 });

			list.add(createCp1252("\nORANGE\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 }); // Cancel Character Expansion

			list.add(createCp1252("36 AVENUE LA MOTTE PICQUET\r\n\r\n"));

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("Date   : MM/DD/YYYY   Heure: HH:MM\r\n"));

			list.add(createCp1252("Boutique: OLUA23      Caisse: 0001\r\n"));
			list.add(createCp1252("Conseiller: 002970    Ticket: 3881\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("Vous avez été servi par : Souad\r\n\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE\r\n"));

			list.add(createCp1252("3700615033581        1        X        19.99€                 19.99€\r\n\r\n"));

			list.add(createCp1252("dont contribution environnementale :\r\n"));

			list.add(createCp1252("CAC IPHONE ORANGE                                              0.01€\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("         1 Piéce(s)        Total :                            19.99€\r\n"));

			list.add(createCp1252("                Mastercard Visa  :                            19.99€\r\n\r\n"));

			list.add(createCp1252("Taux TVA    Montant H.T.   T.V.A\r\n"));

			list.add(createCp1252("  20%          16.66€      3.33€\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("Merci de votre visite et. à bientôt. Conservez votre ticket il\r\n"));

			list.add(createCp1252("vous sera demandé pour tout échange.\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 });

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		}

		return sendCommand(context, portName, portSettings, list);
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param strPrintArea
	 *     Printable area size, This should be ("2inch (58mm)" or "3inch (80mm)")
	 */
	public static boolean PrintSampleReceipt_Portuguese(Context context, String portName, String portSettings, String strPrintArea) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();

		if (strPrintArea.equals("2inch (58mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x10 });

			list.add(createCp1252("\nCOMERCIAL DE ALIMENTOS\r\n"));

			list.add(createCp1252("CARREFOUR LTDA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("Avenida Moyses Roysen,\r\n"));

			list.add(createCp1252("S/N Vila Guilherme\r\n"));

			list.add(createCp1252("Cep: 02049-010 – Sao Paulo – SP\r\n"));

			list.add(createCp1252("CNPJ: 62.545.579/0013-69\r\n"));

			list.add(createCp1252("IE:110.819.138.118\r\n"));

			list.add(createCp1252("IM: 9.041.041-5\r\n"));


			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS\r\n"));
			list.add(createCp1252("CCF:133939 COO:227808\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("CUPOM FISCAL\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("001 2505 CAFÉ DO PONTO TRAD A\r\n"));
			list.add(createCp1252("                    1un F1 8,15)\r\n"));

			list.add(createCp1252("002 2505 CAFÉ DO PONTO TRAD A\r\n"));
			list.add(createCp1252("                    1un F1 8,15)\r\n"));

			list.add(createCp1252("003 2505 CAFÉ DO PONTO TRAD A\r\n"));
			list.add(createCp1252("                    1un F1 8,15)\r\n"));

			list.add(createCp1252("004 6129 AGU MIN NESTLE 510ML\r\n"));
			list.add(createCp1252("                    1un F1 1,39)\r\n"));

			list.add(createCp1252("005 6129 AGU MIN NESTLE 510ML\r\n"));
			list.add(createCp1252("                    1un F1 1,39)\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("TOTAL  R$  27,23\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("DINHEIROv                29,00\r\n"));

			list.add(createCp1252("TROCO R$                    1,77\r\n"));

			list.add(createCp1252("Valor dos Tributos R$2,15(7,90%)\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("ITEM(S) CINORADIS 5\r\n"));

			list.add(createCp1252("OP.:15326  PDV:9  BR,BF:93466\r\n"));

			list.add(createCp1252("OBRIGADO PERA PREFERENCIA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("VOLTE SEMPRE!\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("SAC 0800 724 2822\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("MD5:\r\n"));

			list.add(createCp1252("fe028828a532a7dbaf4271155aa4e2db\r\n"));

			list.add(createCp1252("Calypso_CA CA.20.c13\r\n"));

			list.add(createCp1252(" – Unisys Brasil\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("DARUMA AUTOMAÇÃO   MACH 2\r\n"));

			list.add(createCp1252("ECF-IF VERSÃO:01,00,00 ECF:093\r\n"));

			list.add(createCp1252("Lj:0204 OPR:ANGELA JORGE\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("DDDDDDDDDAEHFGBFCC\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS\r\n"));

			list.add(createCp1252("FAB:DR0911BR000000275026\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("3inch (80mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x10 });

			list.add(createCp1252("\nCOMERCIAL DE ALIMENTOS\r\n"));

			list.add(createCp1252("CARREFOUR LTDA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("Avenida Moyses Roysen, S/N Vila Guilherme\r\n"));

			list.add(createCp1252("Cep: 02049-010 – Sao Paulo – SP\r\n"));

			list.add(createCp1252("CNPJ: 62.545.579/0013-69\r\n"));

			list.add(createCp1252("IE:110.819.138.118  IM: 9.041.041-5\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS  CCF:133939 COO:227808\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("CUPOM FISCAL\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("001 2505 CAFÉ DO PONTO TRAD A       1un F1 8,15)\r\n"));

			list.add(createCp1252("002 2505 CAFÉ DO PONTO TRAD A       1un F1 8,15)\r\n"));

			list.add(createCp1252("003 2505 CAFÉ DO PONTO TRAD A       1un F1 8,15)\r\n"));

			list.add(createCp1252("004 6129 AGU MIN NESTLE 510ML       1un F1 1,39)\r\n"));

			list.add(createCp1252("005 6129 AGU MIN NESTLE 510ML       1un F1 1,39)\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("TOTAL  R$  27,23\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Alignment

			list.add(createCp1252("DINHEIROv                29,00\r\n"));

			list.add(createCp1252("TROCO R$                  1,77\r\n"));

			list.add(createCp1252("Valor dos Tributos R$2,15(7,90%)\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("ITEM(S) CINORADIS 5\r\n"));

			list.add(createCp1252("OP.:15326  PDV:9  BR,BF:93466\r\n"));

			list.add(createCp1252("OBRIGADO PERA PREFERENCIA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("VOLTE SEMPRE!\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("SAC 0800 724 2822\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("MD5: fe028828a532a7dbaf4271155aa4e2db\r\n"));

			list.add(createCp1252("Calypso_CA CA.20.c13  – Unisys Brasil\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("DARUMA AUTOMAÇÃO   MACH 2\r\n"));

			list.add(createCp1252("ECF-IF VERSÃO:01,00,00 ECF:093\r\n"));

			list.add(createCp1252("Lj:0204 OPR:ANGELA JORGE\r\n"));

			list.add(createCp1252("DDDDDDDDDAEHFGBFCC\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS\r\n"));

			list.add(createCp1252("FAB:DR0911BR000000275026\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("4inch (112mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x10 });

			list.add(createCp1252("\nCOMERCIAL DE ALIMENTOS CARREFOUR LTDA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("Avenida Moyses Roysen, S/N Vila Guilherme\r\n"));

			list.add(createCp1252("Cep: 02049-010 – Sao Paulo – SP\r\n"));

			list.add(createCp1252("CNPJ: 62.545.579/0013-69\r\n"));

			list.add(createCp1252("IE:110.819.138.118  IM: 9.041.041-5\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS  CCF:133939 COO:227808\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("CUPOM FISCAL\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("001   2505     CAFÉ DO PONTO TRAD A                      1un F1 8,15)\r\n"));

			list.add(createCp1252("002   2505     CAFÉ DO PONTO TRAD A                      1un F1 8,15)\r\n"));

			list.add(createCp1252("003   2505     CAFÉ DO PONTO TRAD A                      1un F1 8,15)\r\n"));

			list.add(createCp1252("004   6129     AGU MIN NESTLE 510ML                      1un F1 1,39)\r\n"));

			list.add(createCp1252("005   6129     AGU MIN NESTLE 510ML                      1un F1 1,39)\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("TOTAL  R$  27,23\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Alignment

			list.add(createCp1252("DINHEIROv                29,00\r\n"));

			list.add(createCp1252("TROCO R$                  1,77\r\n"));

			list.add(createCp1252("Valor dos Tributos R$2,15(7,90%)\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("ITEM(S) CINORADIS 5\r\n"));

			list.add(createCp1252("OP.:15326  PDV:9  BR,BF:93466\r\n"));

			list.add(createCp1252("OBRIGADO PERA PREFERENCIA.\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("VOLTE SEMPRE!\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("SAC 0800 724 2822\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("MD5: fe028828a532a7dbaf4271155aa4e2db\r\n"));

			list.add(createCp1252("Calypso_CA CA.20.c13  – Unisys Brasil\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("DARUMA AUTOMAÇÃO   MACH 2\r\n"));

			list.add(createCp1252("ECF-IF VERSÃO:01,00,00 ECF:093\r\n"));

			list.add(createCp1252("Lj:0204 OPR:ANGELA JORGE\r\n"));

			list.add(createCp1252("DDDDDDDDDAEHFGBFCC\r\n"));

			list.add(createCp1252("MM/DD/YYYY HH:MM:SS\r\n"));

			list.add(createCp1252("FAB:DR0911BR000000275026\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		}

		return sendCommand(context, portName, portSettings, list);
	}

	/**
	 * This function shows how to read the MSR data(credit card) of a portable(ESC/POS) printer. The function first puts the printer into MSR read mode, then asks the user to swipe a credit card The function waits for a response from the user. The user can cancel MSR mode or have the printer read the card.
	 *
	 * @param context
	 *     Activity for displaying messages to the user
	 * @param portName
	 *     Port name to use for communication. This should be (TCP:<IPAddress> or BT:<Device pair name>)
	 * @param portSettings
	 *     Should be portable;escpos, the port settings portable;escpos is used for portable(ESC/POS) printers
	 * @param strPrintArea
	 *     Printable area size, This should be ("2inch (58mm)" or "3inch (80mm)")
	 */
	public static boolean PrintSampleReceipt_Spanish(Context context, String portName, String portSettings, String strPrintArea) {
		ArrayList<byte[]> list = new ArrayList<byte[]>();

		if (strPrintArea.equals("2inch (58mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[]{0x1d, 0x21, 0x11});

			list.add(createCp1252("BAR RESTAURANT\r\n"));

			list.add(createCp1252("EL POZO\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("C/.ROCAFORT 187\r\n"));

			list.add(createCp1252("08029 BARCELONA\r\n\r\n"));

			list.add(createCp1252("NIF :X-3856907Z\r\n"));

			list.add(createCp1252("TEL :934199465\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("MESA: 100 P: - FECHA: YYYY-MM-DD\r\n"));
			list.add(createCp1252("CAN P/U DESCRIPCION  SUMA\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252(" 4  3,00  JARRA  CERVESA   12,00\r\n"));

			list.add(createCp1252(" 1  1,60  COPA DE CERVESA   1,60\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(createCp1252("               SUB TOTAL : 13,60\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Alignment

			list.add(createCp1252("TOTAL:     13,60 EUROS\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[]{0x1d, 0x21, 0x00});

			list.add(createCp1252("NO: 000018851     IVA INCLUIDO\r\n"));

			list.add(createCp1252("--------------------------------\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("**** GRACIAS POR SU VISITA! ****\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("3inch (80mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x11 });

			list.add(createCp1252("BAR RESTAURANT EL POZO\r\n"));

			list.add(new byte[]{0x1d, 0x21, 0x00});

			list.add(createCp1252("C/.ROCAFORT 187 08029 BARCELONA\r\n"));

			list.add(createCp1252("NIF :X-3856907Z  TEL :934199465\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252("MESA: 100 P: - FECHA: YYYY-MM-DD\r\n"));

			list.add(createCp1252("CAN P/U DESCRIPCION  SUMA\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(createCp1252(" 4    3,00      JARRA  CERVESA             12,00\r\n"));

			list.add(createCp1252(" 1    1,60      COPA DE CERVESA             1,60\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Alignment

			list.add(createCp1252("SUB TOTAL : 13,60\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("TOTAL:     13,60 EUROS\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[]{0x1d, 0x21, 0x00});

			list.add(createCp1252("NO: 000018851     IVA INCLUIDO\r\n"));

			list.add(createCp1252("------------------------------------------------\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("**** GRACIAS POR SU VISITA! ****\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		} else if (strPrintArea.equals("4inch (112mm)")) {
			list.add(new byte[] { 0x1c, 0x43, 0x00 }); // Disable Kanji mode

			list.add(new byte[] { 0x1b, 0x74, 0x11 }); // Code Page #1252 (Windows Latin-1)

			list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			// list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());

			// list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

			// Notice that we use a unicode representation because that is
			// how Java expresses these bytes as double byte unicode

			// Character expansion
			list.add(new byte[] { 0x1d, 0x21, 0x11 });

			list.add(createCp1252("BAR RESTAURANT EL POZO\r\n"));

			list.add(new byte[]{0x1d, 0x21, 0x00});

			list.add(createCp1252("C/.ROCAFORT 187 08029 BARCELONA\r\n"));

			list.add(createCp1252("NIF :X-3856907Z  TEL :934199465\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252("MESA: 100 P: - FECHA: YYYY-MM-DD\r\n"));

			list.add(createCp1252("CAN P/U DESCRIPCION  SUMA\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(createCp1252(" 4        3,00          JARRA  CERVESA                          12,00\r\n"));

			list.add(createCp1252(" 1        1,60          COPA DE CERVESA                          1,60\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x02 }); // Alignment

			list.add(createCp1252("SUB TOTAL         :     13,60\r\n"));

			list.add(new byte[] { 0x1d, 0x21, 0x01 });

			list.add(createCp1252("TOTAL    :    13,60 EUROS\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			list.add(new byte[] { 0x1d, 0x21, 0x00 });

			list.add(createCp1252("NO: 000018851                 IVA INCLUIDO\r\n"));

			list.add(createCp1252("---------------------------------------------------------------------\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x01 }); // Alignment (center)

			list.add(createCp1252("**** GRACIAS POR SU VISITA! ****\r\n"));

			list.add(new byte[] { 0x1b, 0x61, 0x00 }); // Alignment

			byte[] qrcodeByteBuffer = new byte[] {
					0x1d, 0x5a, 0x02, 0x1b, 0x5a, 0x00, 0x51, 0x04, 0x1C, 0x00,
					0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
					0x2e, 0x53, 0x74, 0x61, 0x72, 0x4d, 0x69, 0x63, 0x72, 0x6f,
					0x6e, 0x69, 0x63, 0x73, 0x2e, 0x63, 0x6f, 0x6d };
			list.add(qrcodeByteBuffer); // QR Code (View QR 2D Barcode code for better explanation)

			list.add("\n\n\n\n".getBytes());

		}

		return sendCommand(context, portName, portSettings, list);
	}



	private static byte[] createShiftJIS(String inputText) {
		byte[] byteBuffer = null;

		try {
			byteBuffer = inputText.getBytes("Shift_JIS");
		} catch (UnsupportedEncodingException e) {
			byteBuffer = inputText.getBytes();
		}

		return byteBuffer;
	}

	private static byte[] createBIG5(String inputText) {
		byte[] byteBuffer = null;

		try {
			byteBuffer = inputText.getBytes("Big5");
		} catch (UnsupportedEncodingException e) {
			byteBuffer = inputText.getBytes();
		}

		return byteBuffer;
	}

	private static byte[] createCp1252(String inputText) {
		byte[] byteBuffer = null;

		try {
			byteBuffer = inputText.getBytes("Windows-1252");
		} catch (UnsupportedEncodingException e) {
			byteBuffer = inputText.getBytes();
		}

		return byteBuffer;
	}

	private static byte[] convertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
		int dataLength = 0;
		for (int i = 0; i < ByteArray.size(); i++) {
			dataLength += ByteArray.get(i).length;
		}

		int distPosition = 0;
		byte[] byteArray = new byte[dataLength];
		for (int i = 0; i < ByteArray.size(); i++) {
			System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
			distPosition += ByteArray.get(i).length;
		}

		return byteArray;
	}

	/*
	 * private static void checkPrinterSendToComplete(StarIOPort port) throws StarIOPortException { int timeout = 20000; long timeCount = 0; int readSize = 0; byte[] statusCommand = new byte[] { 0x1b, 0x76 }; byte[] statusReadByte = new byte[] { 0x00 };
	 * try { port.writePort(statusCommand, 0, statusCommand.length);
	 * StarPrinterStatus status = port.retreiveStatus();
	 * if (status.coverOpen) { throw new StarIOPortException("printer is cover open"); } if (status.receiptPaperEmpty) { throw new StarIOPortException("paper is empty"); } if (status.offline) { throw new StarIOPortException("printer is offline"); }
	 * long timeStart = System.currentTimeMillis();
	 * while (timeCount < timeout) { readSize = port.readPort(statusReadByte, 0, 1);
	 * if (readSize == 1) { break; }
	 * timeCount = System.currentTimeMillis() - timeStart; } } catch (StarIOPortException e) { try { try { Thread.sleep(500); } catch(InterruptedException ie) {}
	 * StarPrinterStatus status = port.retreiveStatus(); if (status.coverOpen) { throw new StarIOPortException("printer is cover open"); } if (status.receiptPaperEmpty) { throw new StarIOPortException("paper is empty"); } if (status.offline) { throw new StarIOPortException("printer is offline"); }
	 * long timeStart = System.currentTimeMillis();
	 * while (timeCount < timeout) { readSize = port.readPort(statusReadByte, 0, 1);
	 * if (readSize == 1) { break; }
	 * timeCount = System.currentTimeMillis() - timeStart; } } catch (StarIOPortException ex) { throw new StarIOPortException(ex.getMessage()); } } }
	 */

	public static boolean sendCommand(Context context, String portName, String portSettings, ArrayList<byte[]> byteList) {
		boolean result = true;
		StarIOPort port = null;
		try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
			port = StarIOPort.getPort(portName, portSettings, 20000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			/*
			 * portable(ESC/POS) Printer Firmware Version 2.4 later, SM-S220i(Firmware Version 2.0 later)
			 * Using Begin / End Checked Block method for preventing "data detective".
			 * When sending large amounts of raster data, use Begin / End Checked Block method and adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent "timeout" of the "endCheckedBlock method" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "StarIOPort.getPort" method. (e.g.) 10000 -> 30000When use "Begin / End Checked Block Sample Code", do comment out "query commands Sample code".
			 */

			/* Start of Begin / End Checked Block Sample code */
			StarPrinterStatus status = port.beginCheckedBlock();

			if (true == status.offline) {
				throw new StarIOPortException("A printer is offline");
			}

			byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
			port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

			port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout time of endCheckedBlock method.
			status = port.endCheckedBlock();

			if (true == status.coverOpen) {
				throw new StarIOPortException("Printer cover is open");
			} else if (true == status.receiptPaperEmpty) {
				throw new StarIOPortException("Receipt paper is empty");
			} else if (true == status.offline) {
				throw new StarIOPortException("Printer is offline");
			}
			/* End of Begin / End Checked Block Sample code */

			/*
			 * portable(ESC/POS) Printer Firmware Version 2.3 earlier
			 * Using query commands for preventing "data detective".
			 * When sending large amounts of raster data, send query commands after writePort data for confirming the end of printing and adjust the value in the timeout in the "checkPrinterSendToComplete" method in order to prevent "timeout" of the "sending query commands" while a printing.
			 * If receipt print is success but timeout error occurs(Show message which is "There was no response of the printer within the timeout period."), need to change value of timeout more longer in "checkPrinterSendToComplete" method. (e.g.) 10000 -> 30000When use "query commands Sample code", do comment out "Begin / End Checked Block Sample Code".
			 */

			/* Start of query commands Sample code */
			// byte[] commandToSendToPrinter = convertFromListByteArrayTobyteArray(byteList);
			// port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);
			//
			// checkPrinterSendToComplete(port);
			/* End of query commands Sample code */
		} catch (StarIOPortException e) {
			result = false;
			Builder dialog = new AlertDialog.Builder(context);
			dialog.setNegativeButton("Ok", null);
			AlertDialog alert = dialog.create();
			alert.setTitle("Failure");
			alert.setMessage(e.getMessage());
			alert.setCancelable(false);
			alert.show();
		} finally {
			if (port != null) {
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
				}
			}
		}

		return result;
	}
}

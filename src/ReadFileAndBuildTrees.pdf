import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFileAndBuildTrees {

	final int tabStep = 8;
	final int debugLevel = 1;	// 0-No, 1-Minimal, 2-Detailed

	double numErrMargin;
	boolean caseSensitive;
	String[] vBoundaries;
	String[] blockTypes;

	// for storing distinct mismatched output string for analysis
	Set<String> distinctOutputs;

	// some global variables
	String currentFileName = "";
	String sid="";
	String seq="";
	String attempt="";
	String tcid="";


	class Matrix {
		char[][] outputMatrix;
		int row;
		int col;

		public Matrix(int row, int col) {
			this.row = row;
			this.col = col;
			outputMatrix = new char[row][col];
		}
	}

	ReadFileAndBuildTrees(){
		distinctOutputs = new HashSet<String>();
	}

	private String readTextFile(File file) {
		String everything = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return everything;
	}

	// recursively read every file in the given directory
	public void readAllFiles(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				readAllFiles(fileEntry);
			} else {
				System.out.println(fileEntry.getName());
				currentFileName = fileEntry.getName();
				String fileContent = readTextFile(fileEntry);
				readFileLines(fileContent);
			}
		}
	}

	public void readSingleFile(final File fileEntry) {
		System.out.println(fileEntry.getName());
		currentFileName = fileEntry.getName();
		String fileContent = readTextFile(fileEntry);
		readFileLines(fileContent);
	}

	// actually read the file line by line
	public void readFileLines(String content) 
	{
		int BEGIN_LINE = 8;	// the first expected output line
		String currentOutputType = "exp";
		boolean expReadCompleted = false;
		boolean actReadCompleted = false;
		StringBuilder exp = new StringBuilder();
		StringBuilder act = new StringBuilder();

		String[] lines = content.split("\n");	// split the output by new lines

		// read the comparison options from the corresponding lines of the output file
		String[] options = lines[7].split(";");

		// parse the comparison options
		for(String option: options) {
			if(option.startsWith("VBoundaries")) {
				if(option.trim().split("=").length == 2)
					vBoundaries = option.trim().split("=")[1].split(",");
				else
					vBoundaries = new String[0];
			}
			if(option.startsWith("BlockTypes")) {
				if(option.trim().split("=").length == 2)
					blockTypes = option.trim().split("=")[1].split(",");
				else
					blockTypes = new String[0];

			}
			if(option.startsWith("numErrMargin"))
				numErrMargin = Double.parseDouble(option.trim().split("=")[1]);
			if(option.startsWith("caseSensitive"))
				caseSensitive = Boolean.parseBoolean(option.trim().split("=")[1]);
		}

		int processedOutputCount = 0;
		for(int i=BEGIN_LINE; i<lines.length; i++) {
			if(lines[i].startsWith("***###===EXPECTED===")) {
				currentOutputType = "exp";
				// read properties of the expected output
				String[] outputProperties = lines[i].split(";");
				for(String prop: outputProperties) {
					if(prop.startsWith("Sid"))
						sid = prop.trim().split("=")[1];
					if(prop.startsWith("Seq"))
						seq = prop.trim().split("=")[1];
					if(prop.startsWith("Attempt"))
						attempt = prop.trim().split("=")[1];
					if(prop.startsWith("TCID"))
						tcid = prop.trim().split("=")[1];
				}
				continue;
			}
			if(lines[i].startsWith("***###===END===")) {
				expReadCompleted = true;
			}
			if(lines[i].startsWith("+++###===ACTUAL===")) {
				currentOutputType = "act";
				continue;
			}
			if(lines[i].startsWith("+++###===END===")) {
				actReadCompleted = true;
			}
			if(currentOutputType.equalsIgnoreCase("exp") && !expReadCompleted) {
				if(!lines[i].trim().equals(""))	// exclude if it is a blank line
					exp.append(lines[i]);
			}
			if(currentOutputType.equalsIgnoreCase("act") && !actReadCompleted) {
				if(!lines[i].trim().equals(""))
					act.append(lines[i]);
			}

			// after reading both expected and actual outputs
			if(expReadCompleted && actReadCompleted){

				if(debugLevel >= 1)
					System.out.println("Seq:" + seq + ", TCID:" + tcid + ", Attempt:" + attempt + ", SID:" + sid);

				buildOutputBlocks(exp, act);
				// flush the processed outputs and read the next ones
				processedOutputCount++;
				exp.setLength(0);
				act.setLength(0);
				expReadCompleted = false;
				actReadCompleted = false;
			}

		}
		System.out.println("Processed output count: " + processedOutputCount);
	}

	public void traverseNCompare(TreeNode<String> expRoot, TreeNode<String> actRoot)
	{
		if(expRoot.level.equalsIgnoreCase("L0") && actRoot.level.equalsIgnoreCase("L0"))
			System.out.println("Traversing L0");
		else if(expRoot.level.equalsIgnoreCase("L1") && actRoot.level.equalsIgnoreCase("L1"))
			System.out.println("Traversing L1");
		else if(expRoot.level.equalsIgnoreCase("L2") && actRoot.level.equalsIgnoreCase("L2"))
			System.out.println("Traversing L2");
		else if(expRoot.level.equalsIgnoreCase("L3") && actRoot.level.equalsIgnoreCase("L3"))
		{
			boolean matched = false;
			String type = "";
			if(expRoot.type.equals("DOUBLE") && actRoot.type.equals("DOUBLE")) {
				type = "double";
				matched = Math.abs(Double.parseDouble(expRoot.data) - Double.parseDouble(actRoot.data)) < numErrMargin;
			}
			else if(expRoot.type.equals("INT") && actRoot.type.equals("INT")) {
				type = "int";
				matched = Integer.parseInt(expRoot.data) == Integer.parseInt(actRoot.data);
			}
			else if(expRoot.type.equals("STR") && actRoot.type.equals("STR"))
			{
				if(expRoot.data.trim().split("\\s").length > 1 || actRoot.data.trim().split("\\s").length > 1)	// if either string has more than one elements, NLP
				{
					type = "NLP string";

					// to be substituted by NLP algorithm
					matched = expRoot.data.trim().equalsIgnoreCase(actRoot.data.trim());


					// write mismatched output pairs to file for further analysis
					if(!matched)
						distinctOutputs.add(currentFileName + ";" + tcid + ";" + expRoot.data.trim() + ";" + actRoot.data.trim());
				}
				else
				{
					// ordinary string matching
					type = "string";
					if(caseSensitive)
						matched = expRoot.data.trim().equals(actRoot.data.trim());
					else
						matched = expRoot.data.trim().equalsIgnoreCase(actRoot.data.trim());
				}
			}
			System.out.print("Comparing (" + type + "): \"" + expRoot.data + "\",\"" + actRoot.data + "\" -> ");
			System.out.println(matched);
		}
		else
			System.err.println("Two subtrees are not symmetric - failed to compare");

		// traverse child nodes
		if(expRoot.children.size() != actRoot.children.size())
			System.err.println("Two subtrees are not symmetric - failed to compare");
		else
			for(int i=0; i<expRoot.children.size(); i++)
			{
				traverseNCompare(expRoot.children.get(i), actRoot.children.get(i));
			}
	}


	private void buildOutputBlocks(StringBuilder exp, StringBuilder act) {

		// for storing output blocks
		List<String> expBlocks = new ArrayList<String>();
		List<String> actBlocks = new ArrayList<String>();

		// split the outputs by lines
		String[] expLines = exp.toString().trim().split("[\\r\\n]");
		String[] actLines = act.toString().trim().split("[\\r\\n]");

		// if two outputs have different numbers of lines, stop.
		if(expLines.length != actLines.length) {
			System.err.println("Two outputs cannot be matched, numbers of rows are different.");
			return;
		}

		// store the vertical boundaries using a list 
		List<Integer> vBounds = new ArrayList<Integer>();
		for(String b: vBoundaries)
			vBounds.add(Integer.parseInt(b));

		// output block lines
		String expBlockOfLines = "";
		String actBlockOfLines = "";

		// read each block and place it in the list
		for(int i=0; i<expLines.length; i++)
		{
			expBlockOfLines += (expLines[i] + "\n");
			actBlockOfLines += (actLines[i] + "\n");
			if(vBounds.contains(i) || i==expLines.length-1) {
				expBlocks.add(expBlockOfLines);
				actBlocks.add(actBlockOfLines);
				expBlockOfLines = "";
				actBlockOfLines = "";
			}
		}

		// create tree roots
		TreeNode<String> eRoot = new TreeNode<String>("*root (expected)*","","L0",0,-1,-1,"");
		if(debugLevel >= 1)
			System.out.println("*(L0) build tree root for the expected output: ");
		// read each block
		for(int i=0; i<expBlocks.size(); i++){
			String blockType = blockTypes[i];
			String eBlockLines = expBlocks.get(i);

			if(blockType.equals("g")) {	// general
				eRoot = buildTreeForGeneralBlock(eBlockLines, eRoot, i);
			}
			if(blockType.equals("f")) {	// formatted
				eRoot = buildTreeForFormattedBlock(eBlockLines, eRoot, i);
			}
		}

		if(debugLevel >= 1)
			System.out.println();

		TreeNode<String> aRoot = new TreeNode<String>("*root (actual)*","","L0",0,-1,-1,"");
		if(debugLevel >= 1)
			System.out.println("*(L0) build tree root for the actual output: ");
		// read each block
		for(int i=0; i<actBlocks.size(); i++){
			String blockType = blockTypes[i];
			String aBlockLines = actBlocks.get(i);

			if(blockType.equals("g")) {	// general
				aRoot = buildTreeForGeneralBlock(aBlockLines, aRoot, i);
			}
			if(blockType.equals("f")) {	// formatted
				aRoot = buildTreeForFormattedBlock(aBlockLines, aRoot, i);
			}
		}

		if(debugLevel >= 1)
			System.out.println("\nTraverse trees and compare nodes");
		traverseNCompare(eRoot, aRoot);
		if(debugLevel >= 1)
			System.out.println("Done compare");
	}

	private TreeNode<String> buildTreeForGeneralBlock(String blockOfLines, TreeNode<String> root, int blockID) {
		TreeNode<String> blockRoot = root.addChild("*Block*","","L1",blockID,-1,-1,"");
		if(debugLevel >= 1)
			System.out.println("|-(L1) build subtree for a general block: ");
		int lineID = 0;
		String[] lines = blockOfLines.trim().split("\\n");
		for(String line: lines) {
			TreeNode<String> lineRoot = blockRoot.addChild("*Line*","","L2",lineID++,-1,-1,"");
			if(debugLevel >= 1)
				System.out.println(" |-(L2) build subtree for a line: ");
			String[] tokens = line.split("[:]");
			int tokenId = 0;
			for(String token: tokens) {
				lineRoot.addChild(token.trim(),getType(token.trim()),"L3",tokenId++,-1,-1,getRule(token.trim()));
				if(debugLevel >= 1)
					System.out.println("  |-(L3) add node: " + token.trim());
			}
		}
		return root;
	}


	private TreeNode<String> buildTreeForFormattedBlock(String blockOfLines, TreeNode<String> root, int blockID) {
		TreeNode<String> blockRoot = root.addChild("*Block*","","L1",blockID,-1,-1,"");
		if(debugLevel >= 1)
			System.out.println("|-(L1) build subtree for a formatted block: ");

		// determine the boundaries
		char[] bolderElements = new char[] {'-','|','/','\\'};
		char[] delimiters = new char[] {' '};

		List<Character> bolderElementList = new ArrayList<Character>();
		List<Character> delimiterList = new ArrayList<Character>();
		for(char c:bolderElements)
			bolderElementList.add(c);
		for(char c:delimiters)
			delimiterList.add(c);

		Matrix matrix = genOutputMatrix(blockOfLines);
		int rowCount = matrix.row;
		int colCount = matrix.col;
		int rowBounds[] = new int[rowCount];
		int colBounds[] = new int[colCount];
		int rowSpaces[] = new int[rowCount];
		int colSpaces[] = new int[colCount];
		boolean[] cuttingPoints = new boolean[colCount];
		boolean[] rowBolder = new boolean[rowCount];
		boolean[] colBolder = new boolean[colCount];

		int[] rowAdjs = new int[rowCount-1];
		int[] colAdjs = new int[colCount-1];

		for(int i=0;i<rowCount;i++)	{
			if(i<rowCount-1)
				rowAdjs[i] = 0;
			rowBolder[i] = false;
		}
		for(int i=0;i<colCount;i++) {
			if(i<colCount-1)
				colAdjs[i] = 0;
			cuttingPoints[i] = false;
			colBolder[i] = false;
		}


		for(int i=0;i<rowCount;i++){
			for(int j=0;j<colCount;j++){
				if(bolderElementList.contains(matrix.outputMatrix[i][j])) {
					rowBounds[i]++;
					colBounds[j]++;
				}
				if(delimiterList.contains(matrix.outputMatrix[i][j])) {
					rowSpaces[i]++;
					colSpaces[j]++;
				}
			}
		}

		for(int i=0;i<rowCount;i++){
			for(int j=0;j<colCount-1;j++){
				if(delimiterList.contains(matrix.outputMatrix[i][j])|| delimiterList.contains(matrix.outputMatrix[i][j+1])) {
					colAdjs[j]++;
				}
			}
		}

		for(int i=0;i<rowCount-1;i++){
			for(int j=0;j<colCount;j++){
				if(delimiterList.contains(matrix.outputMatrix[i][j])|| delimiterList.contains(matrix.outputMatrix[i+1][j])) {
					rowAdjs[i]++;
				}
			}
		}

		for(int j=0;j<colCount;j++) {
			if(colBounds[j]==rowCount) {	// if a column is full of bolder elements
				if(debugLevel >= 2)
					System.out.print("V");
				colBolder[j] = true;
			}
			else if(j < colCount-1 && colBounds[j] + colAdjs[j] == rowCount) {
				if(debugLevel >= 2)
					System.out.print(" ");
				cuttingPoints[j] = true;
			}
			else
				if(debugLevel >= 2)
					System.out.print(" ");
		}
		if(debugLevel >= 2)
			System.out.println();

		if(debugLevel >= 2) {
			for(int j=0;j<colCount;j++) {
				if(cuttingPoints[j])
					System.out.print("\\");
				else
					System.out.print(" ");
			}
			System.out.println();
		}

		for(int i=0;i<rowCount;i++) {
			if(debugLevel >= 2)
				for(int j=0;j<colCount;j++)
				{
					System.out.print(String.valueOf(matrix.outputMatrix[i][j]));
				}
			if(rowBounds[i]==colCount) {
				if(debugLevel >= 2)
					System.out.print(" <");
				rowBolder[i] = true;
			}
			else if(rowBounds[i] + rowSpaces[i]==colCount) {
				if(debugLevel >= 2)
					System.out.print(" <");
				rowBolder[i] = true;
			}
			//			else if(i < maxRow-1 && rowBounds[i] + rowAdjs[i]==maxCol)
			//				System.out.print(" <");
			if(debugLevel >= 2)
				System.out.println();
		}

		//=============

		int lineID = 0;
		String[] lines = blockOfLines.trim().split("\\n");

		for(int i=0; i<lines.length; i++) {
			if(rowBolder[i])
				continue;
			String line = lines[i].replaceFirst("\\s++$", "");	// remove trailing spaces
			TreeNode<String> lineRoot = blockRoot.addChild("*Line*","","L2",lineID++,-1,-1,"");
			if(debugLevel >= 1)
				System.out.println(" |-(L2) build subtree for a line: ");

			List<String> tokens = splitStringWithCuttingPoints(line, cuttingPoints, colBolder);

			int tokenId = 0;
			for(String token: tokens) {
				lineRoot.addChild(token.trim(),getType(token.trim()),"L3",tokenId++,-1,-1,getRule(token.trim()));
				if(debugLevel >= 1)
					System.out.println("  |-(L3) add node: " + token.trim());
			}
		}
		return root;
	}

	// split an input line w.r.t. the cutting points array
	private List<String> splitStringWithCuttingPoints(String line, boolean[] cuttingPoints, boolean[] colBolders){
		List<String> tokens = new ArrayList<String>();
		String temp = "";
		for(int i=0;i<line.length();i++) {
			if(!cuttingPoints[i] && !colBolders[i]) {
				temp += line.charAt(i);
			}
			else {
				if(!colBolders[i])
					temp += line.charAt(i);
				if(!temp.trim().equals(""))
					tokens.add(temp);
				temp = "";
			}
		}
		if(!temp.trim().equals(""))
			tokens.add(temp);

		return tokens;		
	}

	// convert tabs to the corresponding number of blank spaces
	private String tabToSpace(String oriLine)
	{
		String tabPattern = new String("\\t");
		Pattern tr = Pattern.compile(tabPattern);
		String line = new String(oriLine);
		Matcher m1 = tr.matcher(line);
		while(m1.find()) {
			int leftTabAlign = m1.start() / tabStep;
			int spacesToAdd = (leftTabAlign + 1) * tabStep - m1.start();

			String sp = "";
			for(int i=0;i<spacesToAdd;i++)
				sp += " ";

			line = tr.matcher(line).replaceFirst(sp);
			m1 = tr.matcher(line);
		}		
		return line;		
	}

	// a pre-process that converts lines of outputs to a character matrix (each line is padded to the maximum width)
	public Matrix genOutputMatrix(String outputLines)
	{
		// find the max width of the matrix (consider a calendar, this first row and the last row may be shorter)
		int maxWidth = 0;
		String[] lines = outputLines.trim().split("\\n");
		for(String line : lines)   {
			line = line.replaceAll("\\s+$", "");
			if (tabToSpace(line).length() > maxWidth)
				maxWidth = tabToSpace(line).length();
		}

		Matrix matrix = new Matrix(lines.length, maxWidth);

		int row = 0;
		for(String line : lines)   {
			line = line.replaceAll("\\s+$", "");
			char[] charArr = tabToSpace(line).toCharArray();
			for(int i=0; i<maxWidth; i++)
			{
				if(i < charArr.length)
					matrix.outputMatrix[row][i] = charArr[i];
				else
					matrix.outputMatrix[row][i] = ' ';
			}
			row++;
		}
		return matrix;
	}


	private String getType(String data) {
		String type = "";
		try {
			Double.parseDouble(data);
			if(data.contains("."))
				type = "DOUBLE";
			else
			{
				try {
					Integer.parseInt(data);	// because value "1F" is considered as double which has no ".", treat is as string 
					type = "INT";
				}catch(NumberFormatException e) {
					type = "STR";
				}
			}
		}catch(NumberFormatException e) {
			type = "STR";
		}



		return type;
	}

	private String getRule(String data) {
		String rule = "";

		// reserved for getting specific matching rules from a token

		return rule;
	}

	public void writeDistinctMismatchedOutputs(File file)
	{
		FileOutputStream fos;
		List<String> sortedList = new ArrayList<String>(distinctOutputs);
		try {
			fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			Collections.sort(sortedList);
			bw.write("pid;tcid;expected;actual");
			bw.newLine();
			for(String s: sortedList) {
				bw.write(s);
				bw.newLine();
				System.out.println(s);
			}
			bw.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sortedList.size() + " lines written");
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// modify this line to change output file directory
		String outputFolderPath = "C:\\Users\\Administrator\\Desktop\\outputs\\";
		//String outputFolderPath = "C:\\Users\\Administrator\\Desktop\\output1\\";

		// modify this line to change result file directory
		String resultFilePath = "C:\\Users\\Administrator\\Desktop\\";

		final String[] pids = {"pid_682_Lab05_Q01",	//0
				"pid_683_Lab05_Q03_dayOfWeek",		//1
				"pid_1477_Lab08_Q02_VendMach",
				"pid_1514_Lab13_Q3a_leastCoins",
				"pid_1516_Lab13_Q3b_listLeastCoins",
				"pid_1550_Lab01_Q05_HelloWrd123",
				"pid_1552_Lab01_Q06_HelloWlcm123",
				"pid_1567_Lab02_Q01(c)Mid",
				"pid_1568_Lab02_Q02(b)Oranges",
				"pid_1569_Lab02_Q03_CredUnits",
				"pid_1574_Lab02_Q05",	//10
				"pid_1576_Lab02_Q06(a)",
				"pid_1578_Lab02_Q06(b)",
				"pid_1579_Lab02_Q07(b)",
				"pid_1611_Lab03_Q01",
				"pid_1613_Lab03_Q02",
				"pid_1614_Lab03_Q03_triangle",
				"pid_1618_Lab03_Q04(b)Score_chk",
				"pid_1620_Lab03_Q05",
				"pid_1621_Lab03_Q06",
				"pid_1622_Lab03_Q07(a)Hex2Dec_2Digits",	//20
				"pid_1623_Lab03_Q07_Hex2Dec_00toFF",
				"pid_1648_Lab04_Q04(a)_MulTable",
				"pid_1649_Lab04_Q04(b)_MulTable_Framed",
				"pid_1654_Lab04_Q06_TableOfPowers",
				"pid_1661_Lab05_Q02_MarksArray",
				"pid_1662_Lab05_Q04_nFunctions",
				"pid_1664_Lab05_Q05",
				"pid_1688_Test1_CA1_Q1",
				"pid_1702_Test1_C61_Q4_CntDivisor",
				"pid_1708_Lab06_Q01",	//30
				"pid_1721_Lab06_Q03",
				"pid_1729_Lab07_Q01",
				"pid_1734_Lab07_Q03",
				"pid_1744_Lab07_Q04",
				"pid_1749_Lab07_Q05",
				"pid_1757_Lab08_Q01_DateV3",
				"pid_1758_Lab08_Q03_Search2DArr",
				"pid_1759_Lab08_Q04",
				"pid_1760_Lab08_Q05",
				"pid_1761_Lab08_Q06_CompareAlphaCh",	//40
				"pid_1764_Lab08_Q08_ReverseDigits",
				"pid_1765_Lab08_Q09_ClassifyMarks",
				"pid_1777_Lab09_Q01",
				"pid_1778_Lab09_Q02",
				"pid_1779_Lab09_Q03",
				"pid_1781_Lab09_Q04",
				"pid_1784_Lab10_Q02",
				"pid_1785_Lab10_Q03",
				"pid_1787_Lab10_Q04",
				"pid_1790_Lab11_Q01_first_n_abc",	//50
				"pid_1793_Lab11_Q03_Add_big_num_arr",
				"pid_1794_Lab11_Q04_Add_big_num_class",
				"pid_1803_Lab12_Q03_StudentResult",
				"pid_1805_Lab13_Q01a_containEven",
				"pid_1806_Lab13_Q01b_showRev",
				"pid_1807_Lab13_Q01c_containDigit",
				"pid_1808_Lab13_Q01d_countDigits",
				"pid_1809_Lab13_Q01e_leftMostDigit",
				"pid_1811_Lab13_Q02_binSearch_recursive",
				"pid_1815_Lab13_Q01f_nonDecr"	//60
		};

		int inPid;
		String outFileName;
		System.out.println("Input PID# (0-60) or 999 for print all"); 
		//inPid = Integer.parseInt(System.console().readLine());
		inPid = 999;

		System.out.println("Dump result to file? (\"N\" or fileName for yes");
		//outFileName = System.console().readLine();
		outFileName = "N";

		ReadFileAndBuildTrees reader = new ReadFileAndBuildTrees();

		if(inPid == 999) {
			// read all output files from a given folder
			File outputFolder = new File(outputFolderPath);
			reader.readAllFiles(outputFolder);
		}
		else {
			// read single file
			String outputFilePath = outputFolderPath + pids[inPid] + ".txt";
			File outputFile = new File(outputFilePath);
			reader.readSingleFile(outputFile);
		}

		if(outFileName.trim().equals("N"))
			outFileName = resultFilePath + "mismatched.csv";	// default file name if not given
		File outFile = new File(outFileName);
		reader.writeDistinctMismatchedOutputs(outFile);
	}

}

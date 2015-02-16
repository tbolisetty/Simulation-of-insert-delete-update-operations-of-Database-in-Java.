import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MyDatabase {
	static RandomAccessFile dataFile, idIndexFile, lastNameIndexFile,
			stateIndexFile;
	static Scanner sc = new Scanner(System.in), inputScanner;
	static String first_name = null, last_name = null, company_name = null,
			address = null, city, county, state, zip, phone1, phone12, email,
			web;
	static boolean bexit = true, bsearch = false;
	static String id;
	static long recordSize = 22, positionToId = 0l, positionToNextId = 0l;
	static String search;
	static String id_index, dataRecord;
	static HashMap<String, ArrayList<String>> stateMap = new HashMap<String, ArrayList<String>>();
	static HashMap<String, ArrayList<String>> lastNameMap = new HashMap<String, ArrayList<String>>();
	static int countRounds;

	public static void main(String[] args){
		try{
		dataFile = new RandomAccessFile("data.db", "rw");
		idIndexFile = new RandomAccessFile("id_index.ndx", "rw");
		lastNameIndexFile = new RandomAccessFile("lastName_index.ndx", "rw");
		stateIndexFile = new RandomAccessFile("state_index.ndx", "rw");

		//System.out.println("Select all Records with same State ");
		System.out.println("1.Inserting Record");
		insertdata();
		System.out.println("2.Reading Record with ID");
		readData();
		System.out.println("3.Select all Records with same State ");
		readDataWithState();
		System.out.println("4.Select all Records with same Last Name");
		readDataWithLastName();
		System.out.println("5.Delete Record using ID");
		deleteData();
		System.out.println("6.Modify Record using ID");
		modifyRecord();
		System.out.println("7.Count no.of Records");
		countRecords();
		
		countRounds = 0;
		countRounds++;
		while (bexit) {

			System.out
					.println("Choose your option: \n1.Insert new record \n2.Search record using ID \n3.Search all records with same state\n"
							+ "4.Search all records with same last name \n5.Delete record using ID \n6.Modify record using ID \n"
							+ "7.Count number of records \n8.Reset data file \n9.Exit ");
			int option = sc.nextInt();
			switch (option) {

			case 1:
				insertdata();
				break;
			case 2:
				readData();
				break;
			case 3:
				readDataWithState();
				break;
			case 4:
				readDataWithLastName();
				break;
			case 5:
				deleteData();
				break;
			case 6:
				modifyRecord();

				break;
			case 7:
				countRecords();
				break;
			case 8:
				resetDataFiles();
				break;
			case 9:
				
				bexit = false;
				sc.close();
				inputScanner.close();
				dataFile.close();
				idIndexFile.close();
				stateIndexFile.close();
				lastNameIndexFile.close();
				
				break;

			default:
				break;
			}
		
		}}
		catch(IOException e)
		{
			System.out.println("error");
		}
	}

	private static void resetDataFiles() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("us-500.txt"));
		System.out.println("Deleting data from all files..");
		
		lastNameMap.clear();
		stateMap.clear();
		dataFile.setLength(0);
		idIndexFile.setLength(0);
		stateIndexFile.setLength(0);
		lastNameIndexFile.setLength(0);
		
		System.out.println("Inserting data...");
		while ((dataRecord = br.readLine()) != null) {
			insertValues(dataRecord);		
		}
	}

	public static void insertdata() throws IOException {

		try {
			openAllFiles();
			if (countRounds == 0) {
				System.out
						.println("Please enter the record in comma separated String with double quotes");

				dataRecord = "\"1111\",\"James\",\"Butt\",\"Benton, John B Jr\",\"6649 N Blue Gum St\",\"New Orleans\",\"Orleans\",\"LA\",\"70116\",\"504-621-8927\",\"504-845-1427\",\"jbutt@gmail.com\",\"http://www.bentonjohnbjr.com\"";
				System.out.println(dataRecord);
				insertValues(dataRecord);
			} else {
/*
				System.out.println("Read with File(1) or command Line(2)");
				String inputMode = sc.next();
				if (inputMode.compareTo("1") == 0) {
					BufferedReader br = new BufferedReader(new FileReader(
							"us-500.txt"));
					while ((dataRecord = br.readLine()) != null) {
						insertValues(dataRecord);
					}

				} else */{
					System.out
							.println("please enter the record in comma separated String with double quotes");
					sc.nextLine();
					if (sc.hasNextLine()) {
						dataRecord = sc.nextLine();
					}
					insertValues(dataRecord);

				}

				dataFile.close();
				
				idIndexFile.close();
				lastNameIndexFile.close();
				stateIndexFile.close();
			}
		} catch (IOException e) {
			e.getStackTrace();
			System.out.println("***Error in insert method ***");

		}

	}

	public static void insertValues(String dataRecord) throws IOException {

		// dataRecord=br.readLine();
		inputScanner = new Scanner(dataRecord);
		dataRecord = dataRecord.substring(1, dataRecord.length() - 1);
		String record[] = new String[13];
		Scanner inputScanner = new Scanner(dataRecord);
		inputScanner.useDelimiter("\",\"");
		int count = 0;
		while (inputScanner.hasNext()) {
			record[count] = inputScanner.next();
			// System.out.println(record[count]);
			count++;

		}
		if (count != 13) {
			System.out.println("please enter all details");
			insertdata();
		}

		else {

			int y = 0;

			id = record[y++];
			first_name = record[y++].toLowerCase();
			last_name = record[y++].toLowerCase();
			company_name = record[y++].toLowerCase();
			address = record[y++].toLowerCase();
			city = record[y++].toLowerCase();
			county = record[y++].toLowerCase();
			state = record[y++].toLowerCase();
			zip = record[y++];
			phone1 = record[y++];
			phone12 = record[y++];
			email = record[y++];
			web = record[y++];
			bsearch = searchId(id);
			if (bsearch != true) {
				insert();
				System.out.println("Record with Id "+ id+" is inserted");
			} else {
				System.out.println("Record with same ID " + id
						+ " is present in database\n");
			}

		}
		inputScanner.close();

	}
	public  static void insert() throws IOException
	{
				long dataFileSize = dataFile.length();
				long idFileSize = idIndexFile.length();

				lastNameIndexFile.seek(0);
				stateIndexFile.seek(0);
				// copying the data from lastname file to hashmap
				if (lastNameIndexFile.length() != 0) {
					String mapValue = lastNameIndexFile.readUTF();
					if (mapValue.length() != 2) {
					mapValue = mapValue.substring(1, mapValue.length() - 2);
					// System.out.println(mapValue);
					for (String keyValue : mapValue.split("], ")) {
						String[] pairs = keyValue.split("=\\[");
						lastNameMap.put(pairs[0], new ArrayList<String>());
						for (String pair : pairs[1].split(",")) {
							pair = (pair).replaceAll("\\s+", "");
							lastNameMap.get(pairs[0]).add(pair);
						}

					}
				}
				}

				if (stateIndexFile.length() != 0) {
					String mapValue = stateIndexFile.readUTF();
					if (mapValue.length() != 2) {
					mapValue = mapValue.substring(1, mapValue.length() - 2);
					// System.out.println(mapValue);
					for (String keyValue : mapValue.split("], ")) {
						String[] pairs = keyValue.split("=\\[");
						stateMap.put(pairs[0], new ArrayList<String>());
						for (String pair : pairs[1].split(",")) {
							pair = (pair).replaceAll("\\s+", "");
							stateMap.get(pairs[0]).add(pair);
						}

					}
				}
				}
				

				dataFile.seek(dataFileSize);
				idIndexFile.seek(idFileSize);

				idIndexFile.writeUTF(id);

				addPaddingSpaces(10 - id.length(), idIndexFile);
				byte[] indexByte = ByteBuffer.allocate(10)
						.putLong(dataFileSize).array();
				idIndexFile.write(indexByte);

				// adding the offset of tuple in to last name hashmap
				if (lastNameMap.containsKey(last_name) != true) {
					lastNameMap.put(last_name, new ArrayList<String>());
				}
				lastNameMap.get(last_name).add(
						String.valueOf(dataFileSize).replaceAll("\\s+", ""));

				// adding the offset of tuple in to state hashmap
				if (stateMap.containsKey(state) != true) {
					stateMap.put(state, new ArrayList<String>());
				}
				stateMap.get(state).add(
						String.valueOf(dataFileSize).replaceAll("\\s+", ""));

				// coping last name hashmap to lastname index file
				lastNameIndexFile.setLength(0);
				lastNameIndexFile.writeUTF(lastNameMap.toString());

				// coping state hashmap to state index file
				stateIndexFile.setLength(0);
				stateIndexFile.writeUTF(stateMap.toString());

				dataFile.writeUTF(id);
				addPaddingSpaces(10 - id.length(), dataFile);

				dataFile.writeUTF(first_name);
				//addPaddingSpaces(25 - first_name.length(), dataFile);

				dataFile.writeUTF(last_name);
				//addPaddingSpaces(25 - last_name.length(), dataFile);

				dataFile.writeUTF(company_name);
				//addPaddingSpaces(50 - company_name.length(), dataFile);
				dataFile.writeUTF(address);

				//addPaddingSpaces(100 - address.length(), dataFile);

				dataFile.writeUTF(city);
				//addPaddingSpaces(25 - city.length(), dataFile);

				dataFile.writeUTF(county);
				//addPaddingSpaces(25 - county.length(), dataFile);

				dataFile.writeUTF(state);
				//addPaddingSpaces(25 - state.length(), dataFile);
				
				dataFile.writeUTF(zip);

				inputScanner = new Scanner(phone1);
				inputScanner.useDelimiter("-");
				String number = "";
				while (inputScanner.hasNext()) {
					number = number + inputScanner.next();
				}
				byte[] phone1IndexByte = ByteBuffer.allocate(10)
						.putLong(Long.parseLong(number)).array();
				dataFile.write(phone1IndexByte);

				inputScanner = new Scanner(phone12);
				inputScanner.useDelimiter("-");
				number = "";
				while (inputScanner.hasNext()) {
					number = number + inputScanner.next();
				}
				

				byte[] phone12IndexByte = ByteBuffer.allocate(10)
						.putLong(Long.parseLong(number)).array();
				dataFile.write(phone12IndexByte);

				dataFile.writeUTF(email);
				//addPaddingSpaces(100 - email.length(), dataFile);

				dataFile.writeUTF(web);
				
			} 
	
	
	
	public static void readData() throws FileNotFoundException {
		openAllFiles();
		if (countRounds == 0) {
			System.out.println("Enter the ID to be searched");
			search = "1";
			System.out.println(search);

		} else {

			// long positionToSeek = 0l;
			System.out.println("Enter the ID to be searched");
			search = sc.next().toLowerCase();
		}
		// calling search function to read the offset from idIndexFile
		bsearch = searchId(search);
		if (bsearch == false) {
			System.out.println("no record with ID\n");
		} else {
			printData(positionToId);
			System.out.println("id: " + id + " first_name: " + first_name
					+ " last_name: " + last_name + " company_name: "
					+ company_name + " address: " + address + " city: " + city
					+ " county: " + county + " state: " + state + " zip: "
					+ zip + " phone1: " + phone1 + " phone2: " + phone12
					+ " email: " + email + " web: " + web +"\n");

		}
	}

	public static void printData(long positionToId) {
		try {

			dataFile.seek(positionToId);
			id = dataFile.readUTF();
			// readByteData(10 - id.length(), dataFile);
			long inde = dataFile.getFilePointer() + 10 - id.length();
			dataFile.seek(inde);

			first_name = dataFile.readUTF();
			//readByteData(25 - first_name.length(), dataFile);

			last_name = dataFile.readUTF();
			//readByteData(25 - last_name.length(), dataFile);

			company_name = dataFile.readUTF();
			//readByteData(50 - company_name.length(), dataFile);

			address = dataFile.readUTF();
			//readByteData(100 - address.length(), dataFile);

			city = dataFile.readUTF();
			//readByteData(25 - city.length(), dataFile);

			county = dataFile.readUTF();
			//readByteData(25 - county.length(), dataFile);

			state = dataFile.readUTF();
			//readByteData(25 - state.length(), dataFile);

			zip = dataFile.readUTF();

			phone1 = readByteData(10, dataFile);

			phone12 = readByteData(10, dataFile);

			email = dataFile.readUTF();
			//readByteData(100 - email.length(), dataFile);

			web = dataFile.readUTF();

			/*System.out.println("id: " + id + " first_name: " + first_name
					+ " last_name: " + last_name + " company_name: "
					+ company_name + " address: " + address + " city: " + city
					+ " county: " + county + " state: " + state + " zip: "
					+ zip + " phone1: " + phone1 + " phone2: " + phone12
					+ " email: " + email + " web: " + web +"\n");
*/
		} catch (IOException e) {
			e.getStackTrace();
			System.out.println("***Error ***" + e.toString());
		}
	}

	private static void readDataWithLastName() throws FileNotFoundException {

		openAllFiles();
		String lastNameToSearch;
		if (countRounds == 0) {
			System.out.println("Enter the Last Name");
			lastNameToSearch = "butt";
			System.out.println(lastNameToSearch);
		} else {
			System.out.println("Enter the Last Name");
			lastNameToSearch = sc.next().toLowerCase();
		}

		try {
			if (lastNameIndexFile.length() != 0) {
				String mapValue = lastNameIndexFile.readUTF();
				{
					if (mapValue.length() != 2) {

						mapValue = mapValue.substring(1, mapValue.length() - 2);
						// System.out.println(mapValue);
						for (String keyValue : mapValue.split("], ")) {
							String[] pairs = keyValue.split("=\\[");
							lastNameMap.put(pairs[0], new ArrayList<String>());
							for (String pair : pairs[1].split(",")) {
								lastNameMap.get(pairs[0]).add(pair);
							}

						}
						if (lastNameMap.containsKey(lastNameToSearch)) {

							ArrayList<String> lnames = new ArrayList<String>(
									lnames = lastNameMap.get(lastNameToSearch));

							for (Object address : lnames) {
								String x = ((String) address).replaceAll(
										"\\s+", "");
								printData(Long.valueOf(x));
								System.out.println("id: " + id + " first_name: " + first_name
								+ " last_name: " + last_name + " company_name: "
								+ company_name + " address: " + address + " city: " + city
								+ " county: " + county + " state: " + state + " zip: "
								+ zip + " phone1: " + phone1 + " phone2: " + phone12
								+ " email: " + email + " web: " + web +"\n");
								
							}
						} else {
							System.out.println("No record with such Last Name\n");
						}
					} else {
						System.out.println("no record to show\n");
					}
				}
			} else {
				System.out.println("no record to show\n");
			}
		} catch (IOException e) {
			System.out.println("***Error in ReadDataWithName***" + e);
		}

	}

	private static void readDataWithState() throws FileNotFoundException {
		openAllFiles();
		String stateToSearch;
		if (countRounds == 0) {
			System.out.println("Enter state");
			stateToSearch = "tx";
			System.out.println(stateToSearch);
		} else {
			System.out.println("Enter state");
			stateToSearch = sc.next().toLowerCase();
		}
		// copying the data from lastname file to hashmap

		try {
			if (stateIndexFile.length() != 0) {
				String mapValue = stateIndexFile.readUTF();
				if (mapValue.length() != 2) {
					mapValue = mapValue.substring(1, mapValue.length() - 2);
					// System.out.println(mapValue);
					for (String keyValue : mapValue.split("], ")) {
						String[] pairs = keyValue.split("=\\[");
						stateMap.put(pairs[0], new ArrayList<String>());
						for (String pair : pairs[1].split(",")) {

							stateMap.get(pairs[0]).add(pair);
						}

					}
					if (stateMap.containsKey(stateToSearch)) {
						for (Object address : stateMap.get(stateToSearch)) {
							String x = ((String) address)
									.replaceAll("\\s+", "");
							printData(Long.valueOf(x));
							System.out.println("id: " + id + " first_name: " + first_name
							+ " last_name: " + last_name + " company_name: "
							+ company_name + " address: " + address + " city: " + city
							+ " county: " + county + " state: " + state + " zip: "
							+ zip + " phone1: " + phone1 + " phone2: " + phone12
							+ " email: " + email + " web: " + web +"\n");
						}
					} else {
						System.out.println("No record with such State\n");
					}
				} else {
					System.out.println("no record to show\n");
				}
			} else {
				System.out.println("no record to show\n");
			}
		} catch (IOException e) {
			System.out.println("***Error in ReadDataWithName***" + e);
		}

	}

	// Gets the String data from index files and store it in hashmap
	// Removes the index address which is to be deleted
	public static HashMap<String, ArrayList<String>> hashfunction(RandomAccessFile xFile,
			long offsetAddress) throws IOException {
		xFile.seek(0);
		HashMap<String, ArrayList<String>> xhash = new HashMap<String, ArrayList<String>>();
		// copying the data from lastname file to hashmap
		if (xFile.length() != 0) {
			String mapValue = xFile.readUTF();
			if (mapValue.length() != 2) {
			mapValue = mapValue.substring(1, mapValue.length() - 2);
			// System.out.println(mapValue);
			String idValueofAddress = null;
			int arrayListIndex = 0;
			int count;
			for (String keyValue : mapValue.split("], ")) {
				String[] pairs = keyValue.split("=\\[");
				xhash.put(pairs[0], new ArrayList<String>());
				count = 0;
				for (String pair : pairs[1].split(",")) {

					String p = (pair).replaceAll("\\s+", "");
					if (Integer.parseInt(p) == offsetAddress) {
						arrayListIndex = count;
						idValueofAddress = pairs[0];
						xhash.get(pairs[0]).add(pair);
					} else {
						count++;
						xhash.get(pairs[0]).add(pair);
					}

					/*
					 * if(Integer.parseInt(p)!=offsetAddress) {
					 * xhash.get(pairs[0]).add(pair); }
					 */
				}

			}
			if(idValueofAddress!=null){
			xhash.get(idValueofAddress).remove(arrayListIndex);
			// map2.remove(x);

			ArrayList<String> object = new ArrayList<String>(xhash.get(idValueofAddress));
			if (object.size() == 0) {
				xhash.remove(idValueofAddress);
			}
			}

		}
		}
		
		return xhash;
	}

	private static void deleteData() {
		try {
			openAllFiles();
			String id;
			int positionValues[] = new int[3];
			if (countRounds == 0) {
				System.out.println("Give the Id of record to be deleted");
				id = "1111";
				System.out.println(id);

			} else {
				System.out.println("Give the Id of record to be deleted");
				id = sc.next();
			}
			String nextId;
			long addressOfNextId, addressOfNextId2;
			positionValues = searchIdToDelete(id);

			if (positionValues[1] == 1) {

				lastNameMap = hashfunction(lastNameIndexFile, positionToId);
				lastNameIndexFile.setLength(0);
				lastNameIndexFile.writeUTF(lastNameMap.toString());
				stateMap = hashfunction(stateIndexFile, positionToId);
				stateIndexFile.setLength(0);
				stateIndexFile.writeUTF(stateMap.toString());

				// idIndexFile.seek(idIndexFile.getFilePointer() + 12);
				if((idIndexFile.length()==22)||(idIndexFile.length()==positionValues[0]+22)){
					addressOfNextId2=dataFile.length();
				}
				else{
				idIndexFile.seek(positionValues[0] + 34);
				
				addressOfNextId2 = Long
						.parseLong(readByteData(10, idIndexFile));
				}

				int i = 0;
				int newIndexOffset;
				for (int index = positionValues[0] + 22; index < idIndexFile
						.length(); index += 22) {
					newIndexOffset = index;
					idIndexFile.seek(newIndexOffset);
					nextId = idIndexFile.readUTF();
					idIndexFile.seek(newIndexOffset + 12);
					addressOfNextId = Long.parseLong(readByteData(10,
							idIndexFile));

					idIndexFile.seek(positionValues[0] + i * 22);
					idIndexFile.writeUTF(nextId);
					addPaddingSpaces(10 - nextId.length(), idIndexFile);
					byte[] indexByte = ByteBuffer.allocate(10)
							.putLong(addressOfNextId).array();
					idIndexFile.write(indexByte);
					i++;
				}
				idIndexFile.setLength(idIndexFile.length() - 22);

				dataFile.seek(positionToId);
				/*
				 * System.out.println(dataFile.readUTF());
				 * System.out.println(dataFile.readUTF());
				 */
				addPaddingSpaces((int) (addressOfNextId2 - positionToId),
						dataFile);
				// dataFile.seek(positionToNextId);
				// copyAllData(positionValues[0]);

				System.out.println("Record with ID "+id +" is deleted \n");

			} else {
				System.out.println("ID with value not available\n");
			}
		} catch (IOException e) {
			System.out.println("*** Error in DeleteData ***");
		}

	}
	

	public static void deleteFromData() throws IOException
	{
		String nextId;
		long addressOfNextId, addressOfNextId2;
		int positionValues[] = new int[3];
		positionValues = searchIdToDelete(id);

		//if (positionValues[1] == 1) {
			

			lastNameMap = hashfunction(lastNameIndexFile, positionToId);
			lastNameIndexFile.setLength(0);
			lastNameIndexFile.writeUTF(lastNameMap.toString());
			stateMap = hashfunction(stateIndexFile, positionToId);
			stateIndexFile.setLength(0);
			stateIndexFile.writeUTF(stateMap.toString());

			// idIndexFile.seek(idIndexFile.getFilePointer() + 12);
			if(idIndexFile.length()==22||positionValues[0]==idIndexFile.length()-22){
				addressOfNextId2=dataFile.length();
			}
			else{
			idIndexFile.seek(positionValues[0] + 34);
			
			addressOfNextId2 = Long
					.parseLong(readByteData(10, idIndexFile));
			}

			int i = 0;
			int newIndexOffset;
			for (int index = positionValues[0] + 22; index < idIndexFile
					.length(); index += 22) {
				newIndexOffset = index;
				idIndexFile.seek(newIndexOffset);
				nextId = idIndexFile.readUTF();
				idIndexFile.seek(newIndexOffset + 12);
				addressOfNextId = Long.parseLong(readByteData(10,
						idIndexFile));

				idIndexFile.seek(positionValues[0] + i * 22);
				idIndexFile.writeUTF(nextId);
				addPaddingSpaces(10 - nextId.length(), idIndexFile);
				byte[] indexByte = ByteBuffer.allocate(10)
						.putLong(addressOfNextId).array();
				idIndexFile.write(indexByte);
				i++;
			}
			idIndexFile.setLength(idIndexFile.length() - 22);

			dataFile.seek(positionToId);
			/*
			 * System.out.println(dataFile.readUTF());
			 * System.out.println(dataFile.readUTF());
			 */
			addPaddingSpaces((int) (addressOfNextId2 - positionToId),
					dataFile);
			// dataFile.seek(positionToNextId);
			// copyAllData(positionValues[0]);


	}

	private static void countRecords() throws IOException {
		// TODO Auto-generated method stub
		openAllFiles();

		long idFileLength;
		if (countRounds == 0) {
			idFileLength = idIndexFile.length();
			System.out.println("Total no.of records: " + (idFileLength / 22l)+"\n");
		} else {
			idFileLength = idIndexFile.length();
			System.out.println("Total no.of records: " + (idFileLength / 22l)+"\n");
		}

	}

	private static void modifyRecord() throws FileNotFoundException {
		// TODO Auto-generated method stub
		openAllFiles();
		String Id, fieldName=null, newValue=null;
		if (countRounds == 0) {
			System.out.println("Enter ID");
			Id = "1";
			System.out.println(Id);
			searchId(Id);
			printData(positionToId);
			System.out.println("id: " + id + " first_name: " + first_name
					+ " last_name: " + last_name + " company_name: "
					+ company_name + " address: " + address + " city: " + city
					+ " county: " + county + " state: " + state + " zip: "
					+ zip + " phone1: " + phone1 + " phone2: " + phone12
					+ " email: " + email + " web: " + web +"\n");

			System.out.println("Select one of the following field_names \n1.first_name 2.last_name 3.company_name 4.address 5.city 6.county "
					+ "7.state 8.zip 9.phone1 10.phone2 11.email 12.web");
			fieldName = "2";

			System.out.println(fieldName);
			System.out.println("Enter new value");
			newValue = "jade";
			System.out.println(newValue);
		} else {
			System.out.println("Enter ID");
			Id = sc.next();
			if(searchId(Id))
			{
				printData(positionToId);
				System.out.println("id: " + id + " first_name: " + first_name
						+ " last_name: " + last_name + " company_name: "
						+ company_name + " address: " + address + " city: " + city
						+ " county: " + county + " state: " + state + " zip: "
						+ zip + " phone1: " + phone1 + " phone2: " + phone12
						+ " email: " + email + " web: " + web +"\n");

				System.out.println("Select one of the following field_names \n1.first_name 2.last_name 3.company_name 4.address 5.city 6.county "
						+ "7.state 8.zip 9.phone1 10.phone2 11.email 12.web");
				fieldName = sc.next();

				System.out.println("Enter new value");
				newValue = sc.next();
			}

		}

		try {

			if (searchId(Id)) {
				dataFile.seek(positionToId);
				switch (fieldName) {

				case "1":
					//copy all values from data file
					//printData(positionToId);
					//set the required field value
					first_name=newValue;
					break;
				case "2":
					/*dataFile.seek(positionToId + 39);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(25 - newValue.length(), dataFile);*/
					last_name=newValue;
					break;
				case "3":
					company_name=newValue;
					/*dataFile.seek(positionToId + 66);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(50 - newValue.length(), dataFile);*/
					break;

				case "4":
					address=newValue;				
					
					/*dataFile.seek(positionToId + 118);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(100 - newValue.length(), dataFile);*/
					break;
				case "5":
					city=newValue;
					/*dataFile.seek(positionToId + 220);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(25 - newValue.length(), dataFile);*/
					break;
				case "6":
					county=newValue;/*
					dataFile.seek(positionToId + 247);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(25 - newValue.length(), dataFile);*/
					break;
				case "7":
					
					state=newValue;
					/*dataFile.seek(positionToId + 274);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(25 - newValue.length(), dataFile);*/
					break;
				case "8":
					zip=newValue;
					/*dataFile.seek(positionToId + 301);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(5 - newValue.length(), dataFile);*/
					/*byte[] zipIndexByte = ByteBuffer.allocate(8)
							.putLong(Long.parseLong(newValue)).array();
					dataFile.write(zipIndexByte);
*/
					break;
				case "9":
					phone1=newValue;
/*
					dataFile.seek(positionToId + 306);
					inputScanner = new Scanner(newValue);
					inputScanner.useDelimiter("-");
					String number = "";
					while (inputScanner.hasNext()) {
						number = number + inputScanner.next();
					}
					byte[] phone1IndexByte = ByteBuffer.allocate(10)
							.putLong(Long.parseLong(number)).array();
					dataFile.write(phone1IndexByte);

*/					break;
				case "10":
					phone12=newValue;
					/*dataFile.seek(positionToId + 316);
					inputScanner = new Scanner(newValue);
					inputScanner.useDelimiter("-");
					String number1 = "";
					while (inputScanner.hasNext()) {
						number1 = number1 + inputScanner.next();
					}
					byte[] phone2IndexByte = ByteBuffer.allocate(10)
							.putLong(Long.parseLong(number1)).array();
					dataFile.write(phone2IndexByte);*/
					break;
				case "11":
					email=newValue;
					/*dataFile.seek(positionToId + 326);
					dataFile.writeUTF(newValue);
					addPaddingSpaces(100 - newValue.length(), dataFile);*/
					break;
				case "12":
					web=newValue;
					/*dataFile.seek(positionToId + 438);
					dataFile.writeUTF(newValue);
*/
					break;

				default:
					break;

				}
				
				//delete the record from old location in data file and delete address values from index files
				deleteFromData();
				//insert the record into data file in new position
				insert();
				searchId(Id);
				printData(positionToId);
				
				System.out.println("id: " + id + " first_name: " + first_name
						+ " last_name: " + last_name + " company_name: "
						+ company_name + " address: " + address + " city: " + city
						+ " county: " + county + " state: " + state + " zip: "
						+ zip + " phone1: " + phone1 + " phone2: " + phone12
						+ " email: " + email + " web: " + web +"\n");

			}

			else {
				System.out.println("No Records found with ID " + Id);
			}
		} catch (IOException e) {
			System.out.println("***Error in modifyRecord***");
		}

	}


	public static boolean searchId(String searchId) {
		// long positionToSeek = 0l;
		String id;
		boolean searchFlag = false;
		try {
			long id_fileSize = idIndexFile.length();
			int position = 0;
			idIndexFile.seek(0);
			long numberOfRecords = id_fileSize / recordSize;

			for (int j = 0; j < numberOfRecords; j++) {
				idIndexFile.seek(position);
				id = idIndexFile.readUTF();
				position += 22;
				if (searchId.equals(id)) {
					searchFlag = true;
					idIndexFile.seek(j * 22 + 12);
					positionToId = Long
							.parseLong(readByteData(10, idIndexFile));
					break;
				} else {
					searchFlag = false;
				}
			}
		} catch (IOException e) {
			System.out.println("***Error inside searchID ***");
		}
		return searchFlag;

	}

	public static int[] searchIdToDelete(String searchId) {
		// long positionToSeek = 0l;
		int[] positionValues = new int[3];
		String id;
		boolean searchFlag = false;
		try {
			long id_fileSize = idIndexFile.length();
			int position = 0;
			idIndexFile.seek(0);
			long numberOfRecords = id_fileSize / recordSize;

			for (int j = 0; j < numberOfRecords; j++) {
				idIndexFile.seek(position);
				id = idIndexFile.readUTF();

				if (searchId.equals(id)) {
					searchFlag = true;
					idIndexFile.seek(j * 22 + 12);
					positionToId = Long
							.parseLong(readByteData(10, idIndexFile));
					positionValues[0] = position;
					positionValues[1] = 1;
					break;
				} else {
					searchFlag = false;
					positionValues[1] = 0;
				}
				position += 22;
			}
		} catch (IOException e) {
			System.out.println("***Error inside searchID ***");
		}
		return positionValues;

	}

	/*
	 * public static String copyAllData(int position){ try{
	 * 
	 * String data=null; for(int
	 * index=position;index<idIndexFile.length();index++) { data+=idIndexFile }
	 * } catch(IOException e){
	 * System.out.println("***Error in copyAllData ***"); } }
	 */
	static String readByteData(int size, RandomAccessFile dataFileToWrite) {
		long value = 0l;
		byte[] indexByte = new byte[size];
		try {
			for (int i = 0; i < size; i++) {

				indexByte[i] = dataFileToWrite.readByte();
			}
			ByteBuffer bb = ByteBuffer.wrap(indexByte);
			value = bb.getLong();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("***Error in readByteData***");
		}

		return String.valueOf(value);

	}

	static void addPaddingSpaces(int spaceSize, RandomAccessFile dataFiletoWrite) {
		try {
			for (int i = 0; i < spaceSize; i++) {
				dataFiletoWrite.writeByte(20);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("***Error in paddingSpaces***");
		}

	}

	static void readPaddingSpaces(int spaceSize,
			RandomAccessFile dataFiletoWrite) {
		try {
			for (int i = 0; i < spaceSize; i++) {
				dataFiletoWrite.readByte();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("***Error in paddingSpaces***");
		}

	}

	private static void openAllFiles() throws FileNotFoundException {
		// TODO Auto-generated method stub
		dataFile = new RandomAccessFile("data.db", "rw");
		idIndexFile = new RandomAccessFile("id_index.ndx", "rw");
		lastNameIndexFile = new RandomAccessFile("lastName_index.ndx", "rw");
		stateIndexFile = new RandomAccessFile("state_index.ndx", "rw");
	}

}

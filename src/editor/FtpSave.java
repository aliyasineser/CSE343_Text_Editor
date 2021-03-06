/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
/**

/**
 * uploadToFTP method takes IP, port, user name, user password, new file name, 
 * file password from user and takes file record type default
 * as ".ptf", texts in editor
 *      Clean first & last spaces from some parameters(without passwords) 
 *      Checks if there exist same file name with -new file name- parameter
 *          add a number to end of file name
 *      Upload file to ftp servers root directory
 *      If exist error, returned and showed to user
 *          
 * @author eda arikan
 */
public class FtpSave {

    static final String fileUpdatedSuccessful = "PANDA_TEXT_EDITOR_FILE_UPLOADED_SUCCESSFUL";
    static final String fileUpdatedError = "PANDA_TEXT_EDITOR_FILE_UPLOADED_ERROR";
    static final String loginError = "PANDA_TEXT_EDITOR_FILE_LOGIN_ERROR";
    static final String logoutError = "PANDA_TEXT_EDITOR_FILE_LOGOUT_ERROR";
    static final String emptyValueError = "PANDA_TEXT_EDITOR_FILE_NULL_VALUE_ERROR"; //for empty parameters
    static final String refusedConnection = "PANDA_TEXT_EDITOR_REFUSED_CONNECTION";

    //these variables just for temporary directory and file 
    final String createdDirName = "__DIRECTORY_SAVE___9199999";
    final String createdFileName = "__SAVED_FILE__9199999";

    String returnState;
    String ipAddress;
    Integer portNumber;
    String userID;
    String userPass;
  

    public boolean save() {
        return false;
    }

    /**
     * Default constructor
     */
    public FtpSave() {
        this.ipAddress = "";
        this.portNumber = 0;
        this.userID = "";
        this.userPass = "";
    }

    /**
     * constructor
     * @param ipAddress
     * @param portNumber
     * @param userID
     * @param userPass
     * @param path 
     */
    public FtpSave(String ipAddress, Integer portNumber, String userID, String userPass, String path) {
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.userID = userID;
        this.userPass = userPass;
    }

    /**
     * get ip address
     * @return String
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * set ip address
     * @param ipAddress String
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * get port number
     * @return Integer
     */
    public Integer getPortNumber() {
        return portNumber;
    }

    /**
     * set port number
     * @param portNumber Integer 
     */
    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * get user id for login
     * @return String
     */
    public String getUserID() {
        return userID;
    }

    /**
     * set user id 
     * @param userID String
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * get user password
     * @return String
     */
    public String getUserPass() {
        return userPass;
    }

    /**
     * set user password
     * @param userPass String
     */
    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    /**
     * Upload file with ftp connection 
     * @param ipAddress to connect ip address
     * @param portNumber to connect port
     * @param userID user id for login
     * @param userPass user password for login
     * @param newFileName file name for upload
     * @param htmlText get written text in editor
     * @param filePassword file password
     * @param recordType default as .ptf
     * @return function is succesfull or not as a string
     * @throws InterruptedException 
     */
    public String uploadToFTP(String ipAddress, String portNumber, String userID,
            String userPass, String newFileName, String htmlText,
            String filePassword, String recordType) throws InterruptedException {

        // get an ftpClient object
        FTPClient ftpClient = new FTPClient();
        FileInputStream inputStream;

        //deleting spaces of end of word
        newFileName = rtrim(newFileName);
        newFileName = ltrim(newFileName);

        //localFileName is same with newFileName. But localFileName must be final for FTPFileFilter() function
        final String localFileName = newFileName;

        try {

            //clear spaces at end of variables
            portNumber = rtrim(portNumber);
            userID = rtrim(userID);
            ipAddress = rtrim(ipAddress);
            newFileName = rtrim(newFileName);

            //clear spaces from start
            portNumber = ltrim(portNumber);
            userID = ltrim(userID);
            ipAddress = ltrim(ipAddress);
            newFileName = ltrim(newFileName);

            //check empty values
            if (ipAddress.isEmpty() || portNumber.isEmpty() || userID.isEmpty() || newFileName.isEmpty()) {
                return emptyValueError;
            }

            //strint to int for port number
            int stringtToIntForPortNumber = Integer.parseInt(portNumber);

            // pass directory path on server to connect
            ftpClient.connect(ipAddress, stringtToIntForPortNumber);

            //check reply code
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                returnState = refusedConnection;
            }

            // pass username and password, returned true if authentication is successfull
            boolean login = ftpClient.login(userID, userPass);

            //System.out.println("-" + userID + "-" + userPass + "-");
            if (login) {

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                //get the path of save location
                Path getHomePath = Paths.get(System.getProperty("user.home") + File.separatorChar + "Documents");

                //create a directory
                String directoryPath;
                directoryPath = getHomePath.toString();
                directoryPath += File.separatorChar + createdDirName;
                new File(directoryPath).mkdir();

                //create a file
                String filePath;
                filePath = directoryPath;
                filePath += File.separatorChar + createdFileName + recordType;
                File file = new File(filePath);

                //before upload, save file
                DirSave.save(file, htmlText, filePassword);

                //for upload take the file to inputstream
                inputStream = new FileInputStream(filePath);

                //controle of same files name with remote server
                FTPFileFilter filter = new FTPFileFilter() {
                    @Override
                    public boolean accept(FTPFile ftpFile) {
                        return (ftpFile.isFile() && ftpFile.getName().contains(localFileName) && ftpFile.getName().startsWith(localFileName));
                    }
                };

                //in root directory, if same file name exist then add a number to end of file name
                FTPFile[] result = ftpClient.listFiles("/", filter);
                if (result.length > 0) {
                    //change file name like file(1).txt as added number
                    newFileName += "(";
                    newFileName += String.valueOf(result.length);
                    newFileName += ")";
                }

                //add file name to record type 
                newFileName += recordType;

                //upload file to root directory
                boolean uploaded = ftpClient.storeFile(newFileName, inputStream);

                if (uploaded) {
                    returnState = fileUpdatedSuccessful;
                } else {
                    returnState = fileUpdatedError;
                }

                // logout the user, returned true if logout successfully
                boolean logout = ftpClient.logout();

                inputStream.close();

                //delete the saved file
                File f = new File(directoryPath);
                if (f.exists()) {
                    deleteDirectory(f);
                }

                if (!logout) {
                    returnState = logoutError;
                }

            } else {
                returnState = loginError;
            }

        } catch (NumberFormatException e) {
            return loginError;
        } catch (IllegalArgumentException e) {
            return loginError;
        } catch (FileSystemNotFoundException e) {
            return loginError;
        } catch (SecurityException e) {
            return loginError;
        } catch (NullPointerException e) {
            return loginError;
        } catch (SocketException e) {
            return loginError;
        } catch (IOException e) {
            return loginError;
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                //do nothing
            }
        }

        return returnState;
    }

    /**
     * clean spaces from end of string
     * @param s string
     * @return string 
     */
    private String rtrim(String s) {
        int i = s.length() - 1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0, i + 1);
    }

    /**
     * clean spaces from start
     * @param s string
     * @return string
     */
    private String ltrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    /**
     * delete temporary saved file
     * @param dir directory
     * @return boolean
     */
    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

}

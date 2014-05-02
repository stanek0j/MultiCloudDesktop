package cz.zcu.kiv.multiclouddesktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multiclouddesktop.data.AccountData;
import cz.zcu.kiv.multiclouddesktop.data.AccountDataListCellRenderer;

public class MultiCloudDesktop extends JFrame {

	/** Serialization constant. */
	private static final long serialVersionUID = -1394767380063338580L;

	private static final String APP_NAME = "MultiCloudDesktop";

	private static MultiCloudDesktop window;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void run() {
				window = new MultiCloudDesktop();
				window.setVisible(true);
			}
		});
	}

	public MultiCloudDesktop() {
		setMinimumSize(new Dimension(720, 480));
		setPreferredSize(new Dimension(720, 480));
		setSize(new Dimension(720, 480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		JPanel accountsPanel = new JPanel();
		getContentPane().add(accountsPanel, BorderLayout.WEST);
		accountsPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane accountScrollPane = new JScrollPane();
		accountScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		accountsPanel.add(accountScrollPane);

		AccountData data = new AccountData();
		data.setName("TEST");
		data.setCloud("Dropbox");
		data.setTotalSpace(1234567890L);
		data.setFreeSpace(123456L);
		data.setUsedSpace(1234444434L);
		DefaultListModel<AccountData> accountModel = new DefaultListModel<>();
		accountModel.addElement(data);

		JList<AccountData> accountList = new JList<>();
		AccountDataListCellRenderer renderer = new AccountDataListCellRenderer(accountList.getFont().deriveFont(Font.BOLD, 14.0f), accountList.getFont());
		accountList.setVisibleRowCount(-1);
		accountScrollPane.setViewportView(accountList);
		accountList.setCellRenderer(renderer);
		accountList.setFixedCellWidth(200);
		accountList.setFixedCellHeight(56);
		accountList.setModel(accountModel);
		accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane dataScrollPane = new JScrollPane();
		contentPanel.add(dataScrollPane, BorderLayout.CENTER);

		JList<FileInfo> dataList = new JList<>();
		dataList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		dataList.setVisibleRowCount(-1);
		dataScrollPane.setViewportView(dataList);

		JPanel statusPanel = new JPanel();
		statusPanel.setMaximumSize(new Dimension(32767, 25));
		statusPanel.setBorder(null);
		statusPanel.setPreferredSize(new Dimension(10, 25));
		statusPanel.setMinimumSize(new Dimension(10, 25));
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(0, 0));

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBorder(new EmptyBorder(4, 8, 4, 8));
		statusPanel.add(lblStatus, BorderLayout.CENTER);

		JPanel progressPanel = new JPanel();
		statusPanel.add(progressPanel, BorderLayout.EAST);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(186, 14));
		progressPanel.add(progressBar);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setPreferredSize(new Dimension(127, 22));
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);

		JMenu mnAccount = new JMenu("Account");
		mnAccount.setMargin(new Insets(4, 4, 4, 4));
		mnAccount.setActionCommand("Account");
		menuBar.add(mnAccount);

		JMenuItem mntmAdd = new JMenuItem("Add");
		mntmAdd.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmAdd);

		JMenuItem mntmAuthorize = new JMenuItem("Authorize");
		mntmAuthorize.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmAuthorize);

		JMenuItem mntmInformation = new JMenuItem("Information");
		mntmInformation.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmInformation);

		JMenuItem mntmQuota = new JMenuItem("Quota");
		mntmQuota.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmQuota);

		JMenuItem mntmRename_1 = new JMenuItem("Rename");
		mnAccount.add(mntmRename_1);

		JMenuItem mntmRemove = new JMenuItem("Remove");
		mntmRemove.setPreferredSize(new Dimension(127, 22));
		mnAccount.add(mntmRemove);

		JMenu mnOperation = new JMenu("Operation");
		mnOperation.setMargin(new Insets(4, 4, 4, 4));
		menuBar.add(mnOperation);

		JMenuItem mntmUpload = new JMenuItem("Upload");
		mntmUpload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JTextField firstName = new JTextField();
				JTextField lastName = new JTextField();
				JPasswordField password = new JPasswordField();
				final JComponent[] inputs = new JComponent[] {
						new JLabel("First"),
						firstName,
						new JLabel("Last"),
						lastName,
						new JLabel("Password"),
						password
				};
				JOptionPane.showMessageDialog(window, inputs, "My custom dialog", JOptionPane.PLAIN_MESSAGE);
				System.out.println("You entered " +
						firstName.getText() + ", " +
						lastName.getText() + ", " +
						String.copyValueOf(password.getPassword()));
			}
		});
		mntmUpload.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmUpload);

		JMenuItem mntmDownload = new JMenuItem("Download");
		mntmDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(window, "???");
			}
		});
		mntmDownload.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmDownload);

		JMenuItem mntmMultiDownload = new JMenuItem("Multi download");
		mnOperation.add(mntmMultiDownload);

		JMenuItem mntmCreateFolder = new JMenuItem("Create folder");
		mntmCreateFolder.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCreateFolder);

		JMenuItem mntmRename = new JMenuItem("Rename");
		mntmRename.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmRename);

		JMenuItem mntmMove = new JMenuItem("Move");
		mntmMove.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmMove);

		JMenuItem mntmCopy = new JMenuItem("Copy");
		mntmCopy.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmCopy);

		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.setPreferredSize(new Dimension(127, 22));
		mnOperation.add(mntmDelete);
	}

}

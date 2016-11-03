KNIME Spotfire Integration
++++++++++++++++++++++++++

Installation
------------
- Install the Spotfire plugin from the KNIME update site (url)
- Go to the org.knime.ext.spotfire_xxx folder
- Open the installation folder 
  ('KNIME root folder'/plugins/org.knime.ext.spotfire_x.x.x/installation)
- Go to the Windows folder and execute setup.exe
  You will need Administration rights in order to register the COM interface

Optional to install the KNIME Spotfire Extension that adds the KNIME hiliting
menu to the Spotfire tools menu (Tools->KNIME HiLite)
- Deploy the KNIMESpotfireExtension.spk file on your Spotifre server
  The file is located in the Spotfire Server folder of the installation folder
  located in the Spotfire plugin folder 
  ('KNIME root folder'/plugins/org.knime.ext.spotfire_x.x.x/installation)

Settings
--------
In order to change standard settings of the Spotfire node within KNIME such as 
the location of the temporary transfer file folder go to 
File->Preferences... open the settings tab KNIME->TIBCO Spotfire.


Uninstall
--------
- Open the Windows Software dialog. Select the KNIME Spotfire COM Bridge 
and press uninstall
- Delete the org.knime.ext.spotfire_xxx folder from the plugins folder of 
your KNIME installation
Optional uninstall the KNIME Spotfire Extension from the Spotfire server.

HiLiting within Spotfire
------------------------
In order to perform hiliting within Spotfire open the 
Tools->KNIME HiLite menu from the Spotfire menu.

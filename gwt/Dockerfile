FROM httpd:2.4
RUN printf 'LoadModule expires_module modules/mod_expires.so\n' >> /usr/local/apache2/conf/httpd.conf\
 && printf '<LocationMatch .*\.nocache\..*>\nExpiresActive on\nExpiresDefault "now"\nHeader merge Cache-Control "public, max-age=0, must-revalidate"\n</LocationMatch>\n' >> /usr/local/apache2/conf/httpd.conf\
 && printf '<LocationMatch .*\.cache\..*>\nExpiresActive on\nExpiresDefault "now plus 1 year"\nHeader merge Cache-Control "public, max-age=31536000, immutable"\n</LocationMatch>\n' >> /usr/local/apache2/conf/httpd.conf
WORKDIR /usr/local/apache2/htdocs/
COPY /target/autorest-nominatim-gwt/example ./
octopus.io Project 
===================

Octopus.io project is for octopus documentation. We use the resulting documentations for hosting the homepage of Octopus project.

Summary
-------

This project uses a document generation tool, Sphinx (http://sphinx-doc.org/index.html). Sphinx uses reStructuredText format and we apply sphinx_rtd_theme for appearance.

Build
-----

The build process for this project is as follows:

1. In order to install sphinx, first you need to install easy_install. You can install it and check the installation as follows (in Centos).

.. code-block:: bash

    $ sudo yum install python-setuptools
    $ easy_install --version

2. Install Sphinx using easy_install.

.. code-block:: bash

    $ sudo easy_install -U Sphinx

3. Build this project using sphinx.

.. code-block:: bash

    $ git clone https://github.com/bitnine-oss/octopus.io.git
    $ cd ocotpus.io
    $ make html


4. You can check the build result by printing _build/html/index.html.

.. code-block:: bash

    $ cd _build/html/
    $ cat index.html





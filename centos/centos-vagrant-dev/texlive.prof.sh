if [ ! -d /usr/local/texlive/2018 ]
  then
    cat <<_EOF_ > texlive.profile
# texlive.profile written on Sat Sep  1 00:07:43 2018 UTC
# It will NOT be updated and reflects only the
# installation profile at installation time.
selected_scheme scheme-full
TEXDIR /usr/local/texlive/2018
TEXMFCONFIG ~/.texlive2018/texmf-config
TEXMFHOME ~/texmf
TEXMFLOCAL /usr/local/texlive/texmf-local
TEXMFSYSCONFIG /usr/local/texlive/2018/texmf-config
TEXMFSYSVAR /usr/local/texlive/2018/texmf-var
TEXMFVAR ~/.texlive2018/texmf-var
binary_x86_64-linux 1
instopt_adjustpath 0
instopt_adjustrepo 1
instopt_letter 1
instopt_portable 0
instopt_write18_restricted 1
tlpdbopt_autobackup 1
tlpdbopt_backupdir tlpkg/backups
tlpdbopt_create_formats 1
tlpdbopt_desktop_integration 1
tlpdbopt_file_assocs 1
tlpdbopt_generate_updmap 0
tlpdbopt_install_docfiles 1
tlpdbopt_install_srcfiles 1
tlpdbopt_post_code 1
tlpdbopt_sys_bin /usr/local/bin
tlpdbopt_sys_info /usr/local/share/info
tlpdbopt_sys_man /usr/local/share/man
tlpdbopt_w32_multi_user 1
_EOF_
fi

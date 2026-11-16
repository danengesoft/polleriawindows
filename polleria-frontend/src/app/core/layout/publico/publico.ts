import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ClienteShell } from './cliente-shell';
@Component({
  selector: 'app-layout-publico',
  imports: [RouterOutlet, ClienteShell],
  templateUrl: './publico.html',
  styleUrl: './publico.css',
})
export class Publico {}

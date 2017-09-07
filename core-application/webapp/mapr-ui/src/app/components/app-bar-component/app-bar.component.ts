import {Component, Input} from "@angular/core";

@Component({
  selector: 'app-bar',
  templateUrl: './app-bar.component.html',
})
export class AppBar {

  @Input()
  sourceURL: string = null;
}
